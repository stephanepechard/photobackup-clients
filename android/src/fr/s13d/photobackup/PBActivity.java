package fr.s13d.photobackup;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.crashlytics.android.Crashlytics;

public class PBActivity extends Activity {

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
        if (item.getItemId() == R.id.action_test_server) {
            settingsFragment.testMediaSender();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
