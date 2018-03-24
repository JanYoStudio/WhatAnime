package pw.janyo.whatanime.util;

import android.content.Context;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;

import pw.janyo.whatanime.R;
import pw.janyo.whatanime.classes.Animation;
import pw.janyo.whatanime.classes.History;
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

	public static <T> T getSavedObject(File file, Class<T> classOfT) {
		if (!file.exists())
			return null;
		try {
			return new Gson().fromJson(new InputStreamReader(new FileInputStream(file)), classOfT);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static List<History> checkList(List<History> list) {
		Iterator<History> iterator = list.iterator();
		while (iterator.hasNext()) {
			final History history = iterator.next();
			File cacheFile = new File(history.getSaveFilePath());
			if (cacheFile.exists())
				continue;
			if (new File(history.getImaPath()).exists()) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						new File(history.getCachePath()).delete();
					}
				}).start();
				continue;
			}
			history.delete();
			iterator.remove();
		}
		return list;
	}

	public static void deleteHistory(History history) {
		new File(history.getCachePath()).deleteOnExit();
		new File(history.getSaveFilePath()).deleteOnExit();
		history.delete();
	}
}
