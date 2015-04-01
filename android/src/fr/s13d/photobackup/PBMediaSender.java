package fr.s13d.photobackup;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import org.apache.http.Header;

import java.io.File;
import java.io.FileNotFoundException;


public class PBMediaSender {

    private final static String LOG_TAG = "PBMediaSender";
    private final static String SERVER_PASS_PARAM = "server_pass";
    private final static String UPFILE_PARAM = "upfile";
    private static AsyncHttpClient syncHttpClient= new SyncHttpClient();
    private static AsyncHttpClient asyncHttpClient = new AsyncHttpClient();


    static public void send(final Context context, final PBMedia media) {
        // Get data
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final String serverUrl = prefs.getString(PBSettingsFragment.PREF_SERVER_URL, "");
        final String serverHash = prefs.getString(PBSettingsFragment.PREF_SERVER_PASS_HASH, "");

        // Create request parameters
        final RequestParams params = new RequestParams();
        params.put(SERVER_PASS_PARAM, serverHash);
        try {
            params.put(UPFILE_PARAM, new File(media.getPath()));
        }
        catch(FileNotFoundException e) {
            e.printStackTrace();
        }

        // Create notification to be used during sending
        final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        final Notification.Builder builder = new Notification.Builder(context);
        builder.setSmallIcon(android.R.drawable.ic_menu_gallery).setLargeIcon(null);

        // Send media
        getClient().post(serverUrl, params, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                // called before request is started
                Log.i(LOG_TAG, context.getResources().getString(R.string.notif_start_title));
                builder.setContentTitle(context.getResources().getString(R.string.notif_start_title))
                       .setContentText(context.getResources().getString(R.string.notif_start_text))
                       .setProgress(100, 0, false);
                notificationManager.notify(0, builder.build());
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                // called when response HTTP status is "200 OK"
                Log.i(LOG_TAG, context.getResources().getString(R.string.notif_success_title));
                builder.setContentTitle(context.getResources().getString(R.string.notif_success_title))
                       .setProgress(0, 0, false);
                notificationManager.notify(0, builder.build());
            }

            @Override
            public void onProgress(int bytesWritten, int totalSize) {
                int progress = (100*bytesWritten) / totalSize;
                builder.setProgress(100, progress, false);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                Log.i(LOG_TAG, context.getResources().getString(R.string.error_uploadfailed));
                builder.setContentTitle(context.getResources().getString(R.string.error_uploadfailed))
                       .setProgress(0, 0, false);
                e.printStackTrace();
            }

        });
    }


    // @return an async client when calling from the main thread, otherwise a sync client.
    private static AsyncHttpClient getClient() {
        // Return the synchronous HTTP client when the thread is not prepared
        if (Looper.myLooper() == null)
            return syncHttpClient;
        return asyncHttpClient;
    }


    public static void test(final Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final String serverUrl = prefs.getString(PBSettingsFragment.PREF_SERVER_URL, "");
        final String serverHash = prefs.getString(PBSettingsFragment.PREF_SERVER_PASS_HASH, "");

        final RequestParams params = new RequestParams(SERVER_PASS_PARAM, serverHash);
        getClient().post(serverUrl + "/test", params, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                Log.i(LOG_TAG, "onSuccess");
                Toast.makeText(context, context.getResources().getString(R.string.toast_configuration_ok), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                Log.i(LOG_TAG, "onFailure");
                Toast.makeText(context, context.getResources().getString(R.string.toast_configuration_ko), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

        });
    }

}
