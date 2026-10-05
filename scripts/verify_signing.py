"""在临时副本中验证签名边界；不读取、修改或复制开发者的签名配置。

需要 Python 3.10+、JDK 21、Android SDK（ANDROID_HOME 或 ANDROID_SDK_ROOT）。
"""

import argparse
import os
from pathlib import Path
import shutil
import subprocess
import tempfile
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
SIGN_KEYS = (
    "SIGN_KEY_STORE_FILE",
    "SIGN_KEY_STORE_PASSWORD",
    "SIGN_KEY_ALIAS",
    "SIGN_KEY_PASSWORD",
)


def run(command, cwd, env):
    result = subprocess.run(
        command, cwd=cwd, env=env, stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT, encoding="utf-8", errors="replace", timeout=900,
    )
    return result.returncode, result.stdout


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--debug", action="store_true", help="同时实际构建 debug APK 并运行单元测试")
    args = parser.parse_args()
    env = dict(os.environ)
    for key in SIGN_KEYS:
        env.pop(key, None)
    if not (env.get("ANDROID_HOME") or env.get("ANDROID_SDK_ROOT")):
        parser.error("请设置 ANDROID_HOME 或 ANDROID_SDK_ROOT；脚本不会读取 local.properties")

    paths = subprocess.check_output(
        ["git", "ls-files", "--cached", "--others", "--exclude-standard", "-z"], cwd=ROOT,
    ).decode("utf-8").split("\0")
    git_dir = subprocess.check_output(
        ["git", "rev-parse", "--absolute-git-dir"], cwd=ROOT, text=True,
    ).strip()
    reports = ROOT / "build/reports/signing"
    if reports.exists():
        shutil.rmtree(reports)
    reports.mkdir(parents=True)
    with tempfile.TemporaryDirectory(prefix="whatanime-signing-") as temporary:
        project = Path(temporary)
        for name in set(paths):
            if not name:
                continue
            path = Path(name)
            # 只复制源码；即使敏感文件被错误跟踪，也不进入测试副本。
            if path.name == "local.properties" or path.suffix.lower() in {".jks", ".keystore", ".p12", ".p8"}:
                continue
            source = ROOT / path
            if source.is_file():
                target = project / path
                target.parent.mkdir(parents=True, exist_ok=True)
                shutil.copy2(source, target)
        # 仅供构建脚本的 rev-list / rev-parse 读取版本；不在副本执行任何 Git 写操作。
        (project / ".git").write_text(f"gitdir: {git_dir}\n", encoding="utf-8")
        gradle = str(project / ("gradlew.bat" if os.name == "nt" else "gradlew"))
        if os.name != "nt":
            (project / "gradlew").chmod(0o755)

        case_number = 0

        def check(label, tasks, marker=None, extra_env=None):
            nonlocal case_number
            case_number += 1
            code, output = run(
                [gradle, *tasks, "--console=plain", "--no-configuration-cache"],
                project, env | (extra_env or {}),
            )
            (reports / f"{case_number:02d}.log").write_text(output, encoding="utf-8")
            for folder in ("test-results/testDebugUnitTest", "reports/tests/testDebugUnitTest"):
                source = project / "composeApp/build" / folder
                if source.is_dir():
                    shutil.copytree(source, reports / folder, dirs_exist_ok=True)
            passed = code == 0 if marker is None else code != 0 and marker in output
            if not passed:
                print(output)
                raise AssertionError(f"{label}: exit={code}, expected={marker or 'success'}")
            print(f"PASS: {label}", flush=True)

        local = project / "local.properties"
        check("没有 local.properties 和 key 时可配置项目", ["help"])
        check("iOS 版本生成不需要 Android key", ["composeApp:updateAppleBuildVersion"])
        local.write_text("# 模拟仅配置 SDK、没有签名的开发环境\n", encoding="utf-8")
        check("存在 local.properties 但无 key 时可配置项目", ["help"])
        if args.debug:
            check("无 key 的 debug APK 和单元测试", [
                "composeApp:testDebugUnitTest", "composeApp:assembleDebug",
            ])
            if not list((project / "composeApp/build/outputs/apk/debug").glob("*.apk")):
                raise AssertionError("debug 任务成功但未生成 APK")
            results = list((project / "composeApp/build/test-results/testDebugUnitTest").glob("TEST-*.xml"))
            suites = [ET.parse(path).getroot() for path in results]
            if not suites or sum(int(s.get("tests", "0")) - int(s.get("skipped", "0")) for s in suites) == 0:
                raise AssertionError("单元测试未实际执行，不能以 NO-SOURCE 视为通过")
        for task in ("assembleRelease", "bundleRelease", "packageRelease", "packageReleaseBundle"):
            check(f"无 key 拒绝 {task}", [f"composeApp:{task}"], "[RELEASE_SIGNING_REQUIRED]")
        if any(
            artifact.suffix in {".apk", ".aab"} and "release" in artifact.parts
            for artifact in (project / "composeApp/build/outputs").rglob("*")
        ):
            raise AssertionError("无 key 时意外生成 release 产物")

        # 显式生成隔离的测试密钥；不参与任何 release 构建或发布。
        keystore = project / "fixture.jks"
        password = "fixture-password"
        java_home = env.get("JAVA_HOME")
        keytool = str(Path(java_home) / "bin" / ("keytool.exe" if os.name == "nt" else "keytool")) if java_home else "keytool"
        code, output = run([
            keytool, "-genkeypair", "-keystore", str(keystore), "-storetype", "JKS",
            "-storepass", password, "-keypass", password, "-alias", "fixture",
            "-keyalg", "RSA", "-keysize", "2048", "-validity", "1",
            "-dname", "CN=Signing Regression Fixture", "-noprompt",
        ], project, env)
        if code:
            raise AssertionError(f"测试密钥生成失败：{output}")
        valid = dict(zip(SIGN_KEYS, (str(keystore), password, "fixture", password)))
        verify = ["composeApp:verifyReleaseSigning"]
        check("本地缺项逐项回退环境变量", verify, extra_env=valid)
        local.write_text("SIGN_KEY_ALIAS=fixture\n", encoding="utf-8")
        check("本地值优先，其余字段回退环境变量", verify, extra_env=valid | {"SIGN_KEY_ALIAS": "wrong"})
        local.write_text("SIGN_KEY_ALIAS=   \n", encoding="utf-8")
        check("空白本地值回退环境变量", verify, extra_env=valid)
        for key in SIGN_KEYS:
            incomplete = dict(valid)
            incomplete.pop(key)
            check(f"缺少 {key} 被拒绝", verify, "[RELEASE_SIGNING_REQUIRED]", incomplete)
        for key, value in (
            ("SIGN_KEY_STORE_FILE", str(project / "missing.jks")),
            ("SIGN_KEY_STORE_PASSWORD", "wrong-password"),
            ("SIGN_KEY_ALIAS", "wrong-alias"),
            ("SIGN_KEY_PASSWORD", "wrong-password"),
        ):
            check(f"无效 {key} 被拒绝", verify, "[RELEASE_SIGNING_INVALID]", valid | {key: value})
        local.write_text("\n".join(f"{key}={value}" for key, value in (
            valid | {"SIGN_KEY_STORE_FILE": "../fixture.jks"}
        ).items()) + "\n", encoding="utf-8")
        check("完整本地配置及模块相对路径", verify)
        local.unlink()
        check("无本地文件时仅使用环境变量", verify, extra_env=valid)
    print("签名边界验证完成；临时副本及测试密钥已删除。")


if __name__ == "__main__":
    main()
