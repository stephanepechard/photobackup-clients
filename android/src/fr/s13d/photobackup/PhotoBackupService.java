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

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.FileObserver;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class PhotoBackupService extends Service {

	private static final String TAG = "PhotoBackupService";
	private FileObserver observer = null;

	/**
	 * Constructor of the PhotoBackupService
	 */
	public PhotoBackupService() {
		MultipartEntity entity = new MultipartEntity(
				HttpMultipartMode.BROWSER_COMPATIBLE);
		try {
			entity.addPart("server_pass", new StringBody("plop"));
		} catch (Exception e) {

		}

		// final String path =
		// android.os.Environment.getExternalStorageDirectory().toString() +
		// "/DCIM/Camera";
		final String path = "/storage/extSdCard/DCIM/Camera";
		// TODO: real detection of system picture directories

		// set up a file observer to watch this directory
		observer = new FileObserver(path) {
			@Override
			public void onEvent(final int event, final String file) {
				// check if its a "create" and not equal to .probe because
				// that's created every time camera is launched
				if ((event == FileObserver.CREATE) && !file.equals(".probe")) {
					Log.v(TAG, "File created [" + path + "/" + file + "]");
					try {
						postPhoto(path + "/" + file);
					} catch (Exception e) {
					}
				}
			}
		};
	}

	/**
	 * Called when an Intent is received via startService().
	 */
	@Override
	public int onStartCommand(final Intent intent, final int flags,
			final int startId) {
		observer.startWatching(); // start the observer
		Log.v(TAG, "FileObserver started");

		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		return START_NOT_STICKY;
	}

	public void postPhoto(final String path) throws Exception {
		// get the user preferences
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		String server_url = sharedPreferences.getString("pref_server_url", "");
		String server_hash = sharedPreferences.getString(
				"pref_server_password_hash", "");

		// create a new HttpClient
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(server_url);

		// create the request
		MultipartEntity entity = new MultipartEntity(
				HttpMultipartMode.BROWSER_COMPATIBLE);
		File upfile = new File(path);
		try {
			entity.addPart("server_pass", new StringBody(server_hash));
			entity.addPart("upfile", new FileBody(upfile));
			httppost.setEntity(entity);
			HttpResponse response = httpclient.execute(httppost);
			int status = response.getStatusLine().getStatusCode();

			System.out.println("Response: " + status);
		} catch (Exception e) {
			entity.addPart("upfile", new StringBody(""));
		}

	}

	@Override
	public void onDestroy() {
		observer.stopWatching();
		Log.v(TAG, "FileObserver stopped");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(final Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
