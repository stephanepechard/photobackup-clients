package fr.s13d.photobackup;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.crashlytics.android.Crashlytics;

public class PBActivity extends Activity {

    private static final String LOG_TAG = "PBActivity";
    private static final PBSettingsFragment settingsFragment = new PBSettingsFragment();


    //////////////
    // Override //
    //////////////
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        Crashlytics.start(this);
        getFragmentManager().beginTransaction().replace(android.R.id.content, settingsFragment).commit();
	}


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.config, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_upload_history) {
            showUploadConfirmationDialog();
            return true;
        } else if (item.getItemId() == R.id.action_test_server) {
            settingsFragment.testMediaSender();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void showUploadConfirmationDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmation");
        builder.setMessage(getMediaStore().getMedias().size() + " pictures have been found, are you sure you want to upload them all?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Log.i(LOG_TAG, "upload_history");
            }
        });
        builder.setNegativeButton("No", null);
        builder.create().show();
    }


    /////////////
    // getters //
    /////////////
    public static PBMediaStore getMediaStore() {
        try {
            return settingsFragment.getService().getMediaStore();
        }
        catch (Exception e) {
            return null;
        }
    }
}
