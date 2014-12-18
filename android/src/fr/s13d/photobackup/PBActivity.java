package fr.s13d.photobackup;

import android.app.Activity;
import android.os.Bundle;

import com.crashlytics.android.Crashlytics;

public class PBActivity extends Activity {

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Crashlytics.start(this);

        PBSettingsFragment settingsFragment = new PBSettingsFragment();
        getFragmentManager().beginTransaction().replace(android.R.id.content, settingsFragment).commit();
	}

}
