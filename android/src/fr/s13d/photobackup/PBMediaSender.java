package fr.s13d.photobackup;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import fr.s13d.photobackup.interfaces.PBMediaSenderInterface;


public class PBMediaSender {

    private final static String LOG_TAG = "PBMediaSender";
    private final static String PASSWORD_PARAM = "password";
    private final static String UPFILE_PARAM = "upfile";
    private final static String TEST_PATH = "/test";
    private static AsyncHttpClient client = new AsyncHttpClient();
    private static List<PBMediaSenderInterface> interfaces = new ArrayList<>();


    public void send(final Context context, final PBMedia media) {
        // Get data
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final String serverUrl = prefs.getString(PBSettingsFragment.PREF_SERVER_URL, "");
        final String serverHash = prefs.getString(PBSettingsFragment.PREF_SERVER_PASS_HASH, "");

        // Create notification to be used during sending
        final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        final Notification.Builder builder = new Notification.Builder(context);
        builder.setSmallIcon(android.R.drawable.ic_menu_upload);
        builder.setLargeIcon(MediaStore.Images.Thumbnails.getThumbnail(context.getContentResolver(),
                media.getId(), MediaStore.Images.Thumbnails.MINI_KIND, null));

        // Create request parameters
        final RequestParams params = new RequestParams();
        params.put(PASSWORD_PARAM, serverHash);
        try {
            params.put(UPFILE_PARAM, new File(media.getPath()));
        }
        catch(FileNotFoundException e) {
            e.printStackTrace();
        }

        // Send media
        client.post(serverUrl, params, new AsyncHttpResponseHandler() {

            @Override // called before request is started
            public void onStart() {
                builder.setContentTitle(context.getResources().getString(R.string.notif_start_title))
                        .setContentText(context.getResources().getString(R.string.notif_start_text))
                        .setProgress(100, 0, false);
                notificationManager.notify(0, builder.build());
                Log.i(LOG_TAG, "START: " + media.getPath());
            }

            @Override // called when response HTTP status is "200 OK"
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                Log.i(LOG_TAG, "SUCCESS: " + media.getPath());
                builder.setContentTitle(context.getResources().getString(R.string.notif_success_title))
                        .setContentText(context.getResources().getString(R.string.notif_success_text))
                        .setSmallIcon(android.R.drawable.ic_menu_slideshow)
                        .setProgress(0, 0, false); // remove it
                notificationManager.notify(0, builder.build());
                media.setState(PBMedia.PBMediaState.SYNCED);
                for (PBMediaSenderInterface senderInterface : interfaces) {
                    senderInterface.onSendSuccess();
                }
            }

            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                final long progress = (100 * bytesWritten) / totalSize;
                builder.setProgress(100, (int) progress, false);
                notificationManager.notify(0, builder.build());
            }

            @Override // called when response HTTP status is "4XX" (eg. 401, 403, 404)
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                Log.i(LOG_TAG, "FAIL: " + media.getPath());
                builder.setContentTitle(context.getResources().getString(R.string.error_uploadfailed))
                        .setContentText(context.getResources().getString(R.string.notif_fail_text))
                        .setSmallIcon(android.R.drawable.ic_dialog_alert)
                        .setProgress(0, 0, false); // remove it
                notificationManager.notify(0, builder.build());
                media.setState(PBMedia.PBMediaState.ERROR);
                for (PBMediaSenderInterface senderInterface : interfaces) {
                    senderInterface.onSendFailure();
                }
                e.printStackTrace();
            }

        });
    }


    public void test(final Context context) {
        Toast.makeText(context, "Testing server", Toast.LENGTH_SHORT).show();

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final String serverUrl = prefs.getString(PBSettingsFragment.PREF_SERVER_URL, "");
        final String serverHash = prefs.getString(PBSettingsFragment.PREF_SERVER_PASS_HASH, "");

        final RequestParams params = new RequestParams(PASSWORD_PARAM, serverHash);
        client.post(serverUrl + TEST_PATH, params, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                for (PBMediaSenderInterface senderInterface : interfaces) {
                    senderInterface.onTestSuccess();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                for (PBMediaSenderInterface senderInterface : interfaces) {
                    senderInterface.onTestFailure();
                }
            }

        });
    }


    public void addInterface(PBMediaSenderInterface senderInterface) {
        interfaces.add(senderInterface);
    }

}
