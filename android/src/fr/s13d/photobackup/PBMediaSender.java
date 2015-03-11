package fr.s13d.photobackup;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import org.apache.http.Header;

import java.io.File;
import java.io.FileNotFoundException;


public class PBMediaSender {

    private final static String LOG_TAG = "PBMediaSender";
    private static AsyncHttpClient syncHttpClient= new SyncHttpClient();
    private static AsyncHttpClient asyncHttpClient = new AsyncHttpClient();


    static public void send(final Context context, final PBMedia media) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final String serverUrl = prefs.getString(PBSettingsFragment.PREF_SERVER_URL, "");
        final String serverHash = prefs.getString(PBSettingsFragment.PREF_SERVER_PASS_HASH, "");

        RequestParams params = new RequestParams();
        params.put("server_pass", serverHash);
        try {
            params.put("upfile", new File(media.getPath()));
        }
        catch(FileNotFoundException e) {
            e.printStackTrace();
        }

        getClient().post(serverUrl, params, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                // called before request is started
                Log.i(LOG_TAG, "onStart");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                // called when response HTTP status is "200 OK"
                Log.i(LOG_TAG, "onSuccess");
            }

            @Override
            public void onProgress(int bytesWritten, int totalSize) {
                Log.i(LOG_TAG, "onProgress: " + ((100*bytesWritten) / totalSize) + "%");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                Log.i(LOG_TAG, "onFailure");
                e.printStackTrace();
            }

        });
    }


    public void notify(final Context context, final String title, final String text) {
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context).setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(title).setContentText(text);

        final Intent notificationIntent = new Intent(context, PBActivity.class);
        final PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
    }


    // @return an async client when calling from the main thread, otherwise a sync client.
    private static AsyncHttpClient getClient() {
        // Return the synchronous HTTP client when the thread is not prepared
        if (Looper.myLooper() == null)
            return syncHttpClient;
        return asyncHttpClient;
    }

}
