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
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

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


public class PBService extends Service {

	private static final String LOG_TAG = "PBService";
    private static SharedPreferences sharedPreferences;
    private static PBService self;
    private MediaContentObserver newMediaContentObserver;


	public PBService() {
        self = this;
    }


    public static PBService getInstance() {
        return self;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        newMediaContentObserver = new MediaContentObserver();
        this.getApplicationContext().getContentResolver().registerContentObserver(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, false, newMediaContentObserver);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        Toast.makeText(this, "PhotoBackup service has started.", Toast.LENGTH_SHORT).show();
        Log.i(LOG_TAG, "start PhotoBackup service");
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        this.getApplicationContext().getContentResolver()
                .unregisterContentObserver(newMediaContentObserver);
        Toast.makeText(this, "PhotoBackup service has stopped.", Toast.LENGTH_SHORT).show();
        Log.i(LOG_TAG, "stop PhotoBackup service");
    }


    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        super.onStartCommand(intent, flags, startId);

        if (intent != null) { // explicitly launch by the user
            Toast.makeText(this, "PhotoBackup service has started.", Toast.LENGTH_SHORT).show();
        }

        return START_STICKY;
    }


    // ContentObserver to react on the creation of a new media
    private class MediaContentObserver extends ContentObserver {

        public MediaContentObserver() {
            super(null);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            PBPicture picture = readFromMediaStore(getApplicationContext(),
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            Log.d(LOG_TAG, "detected picture: " + picture.toString());

            try {
                addPhotoToQueue(picture.getFile().getAbsolutePath());
                picture.save();
            }
            catch (Exception e) {
                PBService.this.notify("Error", "onChange" + e.toString());
            }
        }
    }


    private void addPhotoToQueue(final String fullPath) {
        postPhoto(fullPath);
    }


	private void postPhoto(final String path) {
		// get the user preferences
		String serverUrl = sharedPreferences.getString(PBSettingsFragment.PREF_SERVER_URL, "");
		String serverHash = sharedPreferences.getString(PBSettingsFragment.PREF_SERVER_PASS_HASH, "");

		// create a new HttpClient
		int timeout = 5000; // in milliseconds
		HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, timeout);
		HttpConnectionParams.setSoTimeout(httpParams, timeout);
		HttpClient httpClient = new DefaultHttpClient(httpParams);
		HttpPost httpPost = new HttpPost(serverUrl);

		// create the request
		MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		File upfile = new File(path);
		long uploaded = 0;
		try {
			entity.addPart("server_pass", new StringBody(serverHash));
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
					notify(getResources().getString(R.string.error_uploadfailed), getResources().getString(R.string.error_not200) + " (" + status + ")");
				}
			}
		}
		catch (SocketTimeoutException e) {
			notify(getResources().getString(R.string.error_uploadfailed), getResources().getString(R.string.error_timeout));
		}
		catch (ClientProtocolException e) {
			notify(getResources().getString(R.string.error_uploadfailed), getResources().getString(R.string.error_protocol));
		}
		catch (IOException e) {
			notify(getResources().getString(R.string.error_uploadfailed), getResources().getString(R.string.error_noresponse));
		}
	}


    private void notify(final String title, final String text) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(title).setContentText(text);

        Intent notificationIntent = new Intent(this, PBActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
    }


    private PBPicture readFromMediaStore(Context context, Uri uri) {
        Cursor cursor = context.getContentResolver().query(
                uri, null, null, null, "date_added DESC");
        PBPicture picture = null;
        if (cursor.moveToNext()) {
            int dataColumn = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
            String filePath = cursor.getString(dataColumn);
            picture = new PBPicture(new File(filePath));
        }
        cursor.close();
        return picture;
    }

	@Override
	public IBinder onBind(final Intent intent) {
		return null;
	}

}