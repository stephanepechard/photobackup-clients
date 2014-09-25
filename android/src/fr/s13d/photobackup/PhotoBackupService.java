/**
 * Copyright (C) 2013 Stéphane Péchard.
 * 
 * This file is part of Photo Backup.
 * 
 * Photo Backup is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * Photo Backup is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.s13d.photobackup;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import fr.s13d.photobackup.journal.JournalEntriesDataSource;


public class PhotoBackupService extends Service {

	private static final String LOG_TAG = "PhotoBackupService";
    private SharedPreferences sharedPreferences;
	private RecursiveFileObserver observer = null;
	private final JournalEntriesDataSource datasource;


	public PhotoBackupService() {
		datasource = new JournalEntriesDataSource(this);
	}


    @Override
    public void onCreate() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }


    @Override
    public void onDestroy() {
        if (observer != null) {
            observer.stopWatching();
        }
        Log.v(LOG_TAG, "FileObserver stopped");
    }


    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        super.onStartCommand(intent, flags, startId);

        String pictureDirectory = intent.getStringExtra(PhotoBackupActivity.PICTURE_DIR);
        createObserver(pictureDirectory);

        observer.startWatching(); // start the observer
        Log.v(LOG_TAG, "RecursiveFileObserver started on: " + pictureDirectory);

        createNotification("Information", "Service started");

        return START_STICKY;
    }


    private void createObserver(final String directory) {
		// set up a file observer to watch the given directory
		observer = new RecursiveFileObserver(directory) {

			@Override
			public void onEvent(final int event, final String fullPath) {

                Log.v(LOG_TAG, "onEvent: " + event + " for " + fullPath);
				// Check if it's a CLOSE_WRITE event and not equal
				// to .probe because that's created every time camera
				// is launched or *.tmp because that's created for each picture.
				// Don't use CREATE as it is not finish to write when you get
				// the event.
				if ((event == RecursiveFileObserver.CLOSE_WRITE) && !fullPath.equals(".probe") && !fullPath.endsWith(".tmp")) {
					try {
                        addPhotoToQueue(fullPath);
                        Log.v(LOG_TAG, "File written [" + fullPath + "]");
					}
					catch (Exception e) {
						createNotification("Error", "onEvent" + e.toString());
					}
				}
			}
		};
	}


	private void createNotification(final String title, final String text) {
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.ic_launcher)
		        .setContentTitle(title).setContentText(text);

		Intent notificationIntent = new Intent(this, PhotoBackupActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(contentIntent);

		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(0, builder.build());
	}


    private void addPhotoToQueue(final String fullPath) {
        postPhoto(fullPath);
    }


	private void postPhoto(final String path) {
		// get the user preferences
		String server_url = sharedPreferences.getString(PhotoBackupActivity.PREF_SERVER_URL, "");
		String server_hash = sharedPreferences.getString(PhotoBackupActivity.PREF_SERVER_PASS_HASH, "");

		// create a new HttpClient
		int timeout = 5000; // in milliseconds
		HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, timeout);
		HttpConnectionParams.setSoTimeout(httpParams, timeout);
		HttpClient httpClient = new DefaultHttpClient(httpParams);
		HttpPost httpPost = new HttpPost(server_url);

		// create the request
		MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		File upfile = new File(path);
		long uploaded = 0;
		try {
			entity.addPart("server_pass", new StringBody(server_hash));
			entity.addPart("upfile", new FileBody(upfile));
			httpPost.setEntity(entity);
			HttpResponse response = httpClient.execute(httpPost);
			if (response != null) {
				int status = response.getStatusLine().getStatusCode();
				Log.v(LOG_TAG, "response: " + status);

				if (status == 200) { // success
					uploaded = 1;
				}
				else {
					createNotification(getResources().getString(R.string.error_uploadfailed),
					        getResources().getString(R.string.error_not200) + " (" + status + ")");
				}
			}
		}
		catch (SocketTimeoutException e) {
			createNotification(getResources().getString(R.string.error_uploadfailed),
			        getResources().getString(R.string.error_timeout));
		}
		catch (ClientProtocolException e) {
			createNotification(getResources().getString(R.string.error_uploadfailed),
			        getResources().getString(R.string.error_protocol));
		}
		catch (IOException e) {
			createNotification(getResources().getString(R.string.error_uploadfailed),
			        getResources().getString(R.string.error_noresponse));
		}

		// Add it to the journal
		datasource.open();
		datasource.createEntry(now(), upfile.getAbsolutePath(), uploaded);
		datasource.close();
	}



	private static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
	private static String now() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW, Locale.FRANCE);
		return sdf.format(cal.getTime());
	}


	@Override
	public IBinder onBind(final Intent intent) {
		return null;
	}

}
