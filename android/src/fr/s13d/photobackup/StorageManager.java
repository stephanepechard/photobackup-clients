/**
 * Copyright (C) 2013 Stéphane Péchard.
 *
 * This file is part of Photo Backup.
 *
 * Photo Backup is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Photo Backup is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.s13d.photobackup;

import java.io.File;

import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;

/**
 * 
 */
public class StorageManager {

	public StorageManager() {}

	public static Boolean directoryExists(final String name) {
		File dir = new File(name);
		if (!dir.exists()) {
			return false;
		}
		return true;
	}

	public static String getExternalDcimDirectory() {
		return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();
	}

	public static boolean isExternalStorageMounted() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}

	public static boolean isExternalStorageReadOnly() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			return true;
		}
		return false;
	}

	public static String getPictureDirectory(Context context) {
		String pictureDirectory = new String();

		// Query the media store to find the last picture taken by the user
		String[] projection = new String[]{BaseColumns._ID,
				MediaColumns.DATA,
				MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
				MediaStore.Images.ImageColumns.DATE_TAKEN,
				MediaColumns.MIME_TYPE
		};

		// The most probable is to keep pictures on the internal storage, right?
		final Cursor internalCursor = context.getContentResolver().query(MediaStore.Images.Media.INTERNAL_CONTENT_URI,
				projection, null, null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");
		if (internalCursor.moveToFirst()) {
			File lastPicture = new File(internalCursor.getString(1));
			pictureDirectory = lastPicture.getParentFile().getAbsolutePath();
		} else {
			// if not, try the same on the external storage
			final Cursor externalCursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
					projection, null, null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");
			if (externalCursor.moveToFirst()) {
				File lastPicture = new File(externalCursor.getString(1));
				pictureDirectory = lastPicture.getParentFile().getAbsolutePath();
			}
		}
		return pictureDirectory;
	}
}
