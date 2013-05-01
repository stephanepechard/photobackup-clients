package fr.s13d.photobackup;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceManager;
import android.webkit.URLUtil;
import android.widget.Toast;

public class ConfigActivity extends Activity implements OnSharedPreferenceChangeListener {

	// should correspond to what is in preferences.xml
	private static final String PREF_KEY_SERVICE_RUNNING = "pref_service_running";
	private static final String PREF_KEY_SERVER_URL = "pref_server_url";
	private static final String PREF_KEY_SERVER_PASS = "pref_server_password";
	private static final String PREF_KEY_SERVER_PASS_HASH = "pref_server_password_hash";
	private static final String PREF_KEY_PICTURES_PATH = "pref_pictures_path";
	private static final String PREF_KEY_ONLY_WIFI = "pref_only_wifi";

	private Intent serviceIntent = null;
	private SettingsFragment settingsFragment = null;
	private Boolean hashIsComputed = false;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		serviceIntent = new Intent(this, PhotoBackupService.class);
		settingsFragment = new SettingsFragment();

		// Display the settings fragment as the main content.
		getFragmentManager().beginTransaction().replace(android.R.id.content, settingsFragment).commit();

		// Init the preferences
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		Editor editor = sharedPreferences.edit();
		editor.putBoolean(PREF_KEY_SERVICE_RUNNING, isPhotoBackupServiceRunning());
		// others
		editor.commit();
	}

	@Override
	protected void onResume() {
		super.onResume();
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {

		if (key.equals(PREF_KEY_SERVICE_RUNNING)) {
			// Start/Stop the service

			// TODO: settings validation

			if (sharedPreferences.getBoolean(PREF_KEY_SERVICE_RUNNING, false)) {
				startService(serviceIntent);
			} else {
				stopService(serviceIntent);
			}

		} else if (key.equals(PREF_KEY_SERVER_URL)) {
			// Change the summary to the server URL if it is valid
			String summary = sharedPreferences.getString(PREF_KEY_SERVER_URL, "");

			// Validate URL
			Boolean urlIsValid = true;
			if (!URLUtil.isValidUrl(summary)) {
				Toast.makeText(this, R.string.toast_urisyntaxexception, Toast.LENGTH_SHORT).show();
				urlIsValid = false;
			}

			if (summary.isEmpty() || !urlIsValid) {
				summary = getResources().getString(R.string.server_url_summary);
			}

			// set the summary
			EditTextPreference textPreference = (EditTextPreference) settingsFragment.findPreference(PREF_KEY_SERVER_URL);
			textPreference.setSummary(summary);

		} else if (key.equals(PREF_KEY_SERVER_PASS)) {
			// store only the hash of the password in the preferences
			if (hashIsComputed == false) {
				String pass = sharedPreferences.getString(PREF_KEY_SERVER_PASS, "");

				try {
					// compute the hash
					MessageDigest md = MessageDigest.getInstance("SHA-512");
					md.update(pass.getBytes());
					byte[] mb = md.digest();
					String hash = "";
					for (byte temp : mb) {
						String s = Integer.toHexString(new Byte(temp));
						while (s.length() < 2) {
							s = "0" + s;
						}
						s = s.substring(s.length() - 2);
						hash += s;
					}

					// set the hash in the preferences
					Editor editor = sharedPreferences.edit();
					editor.putString(PREF_KEY_SERVER_PASS_HASH, hash);
					editor.commit();

				} catch (NoSuchAlgorithmException e) {
					System.out.println("ERROR: " + e.getMessage());
				}
			} else {
				hashIsComputed = false;
			}

		} else if (key.equals(PREF_KEY_SERVER_PASS_HASH)) {
			hashIsComputed = true;

			// Remove the real password from the preferences, for security.
			Editor editor = sharedPreferences.edit();
			editor.putString(PREF_KEY_SERVER_PASS, "");
			editor.commit();

		} else if (key.equals(PREF_KEY_PICTURES_PATH)) {

			// Allow the user to modify the directory where the pictures are
			// stored.
			String summary = sharedPreferences.getString(PREF_KEY_PICTURES_PATH, "");
			EditTextPreference textPreference = (EditTextPreference) settingsFragment.findPreference(PREF_KEY_PICTURES_PATH);
			textPreference.setSummary(summary);

		} else if (key.equals(PREF_KEY_ONLY_WIFI)) {
			// Allow the user not to use the mobile network to upload the
			// pictures
			Toast.makeText(this, "This has no effect for the moment :-)", Toast.LENGTH_SHORT).show();

			// TODO: implement
		}
	}

	/**
	 * Returns the current state of the PhotoBackup Service. see
	 * http://stackoverflow.com/a/5921190/417006
	 * 
	 * @return
	 */
	private boolean isPhotoBackupServiceRunning() {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (PhotoBackupService.class.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}
}
