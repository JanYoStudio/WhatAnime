package pw.janyo.whatanime.util;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import vip.mystery0.tools.utils.FileTools;

/**
 * Created by myste.
 */

public class WAFileUtil extends FileTools {
    public static void fileCopy(String oldPath, String newPath) {
        try {
            File oldFile = new File(oldPath);
            if (!oldFile.exists())
                return;
            File newFile = new File(newPath);
            if (!newFile.getParentFile().exists())
                newFile.getParentFile().mkdirs();
            if (!newFile.exists())
                newFile.createNewFile();
            FileInputStream fileInputStream = new FileInputStream(oldFile);
            FileOutputStream fileOutputStream = new FileOutputStream(newFile);
            byte[] bytes = new byte[1024 * 1024 * 10];
            int read;
            while ((read = fileInputStream.read(bytes)) != -1) {
                fileOutputStream.write(bytes, 0, read);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean saveJson(Object object, File file) {
        if (!file.getParentFile().exists())
            file.getParentFile().mkdirs();
        if (file.exists())
            file.delete();
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(new Gson().toJson(object).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (fileOutputStream != null)
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return true;
    }
}
