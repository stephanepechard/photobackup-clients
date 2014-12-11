package fr.s13d.photobackup;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.SwitchPreference;
import android.webkit.URLUtil;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PhotoBackupActivity extends Activity implements OnSharedPreferenceChangeListener {

    private static final String LOG_TAG = "PhotoBackupActivity";

    // should correspond to what is in preferences.xml
    private static final String PREF_SERVICE_RUNNING = "PREF_SERVICE_RUNNING";
    public static final String PREF_SERVER_URL = "PREF_SERVER_URL";
    private static final String PREF_SERVER_PASS = "PREF_SERVER_PASS";
    public static final String PREF_SERVER_PASS_HASH = "PREF_SERVER_PASS_HASH";
    private static final String PREF_ONLY_WIFI = "PREF_ONLY_WIFI";

	private SettingsFragment settingsFragment = null;
	private Boolean hashIsComputed = false;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor sharedPreferencesEditor;


	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Crashlytics.start(this);

    	settingsFragment = new SettingsFragment();
        sharedPreferences = settingsFragment.getPreferenceManager().getSharedPreferences();
        sharedPreferencesEditor = sharedPreferences.edit();
        getFragmentManager().beginTransaction().replace(android.R.id.content, settingsFragment).commit();
	}


	@Override
	protected void onResume() {
		super.onResume();
        if (sharedPreferences != null) {
            sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        }

	}


	@Override
	protected void onPause() {
		super.onPause();
        if (sharedPreferences != null) {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        }
	}


	@Override
	public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
		Log.i(LOG_TAG, "onSharedPreferenceChanged for: " + key);

		if (key.equals(PREF_SERVICE_RUNNING)) {
            Log.i(LOG_TAG, "PREF_SERVICE_RUNNING = " + sharedPreferences.getBoolean(PREF_SERVICE_RUNNING, false));

			// Start/Stop the service
			if (sharedPreferences.getBoolean(PREF_SERVICE_RUNNING, false)) {
				if (validateSettings()) {
                    Log.i(LOG_TAG, "start PhotoBackup service");
                    Intent serviceIntent = new Intent(this, PhotoBackupService.class);
					startService(serviceIntent);
				} else {
					SwitchPreference switchPreference = (SwitchPreference) settingsFragment.findPreference(PREF_SERVICE_RUNNING);
					switchPreference.setChecked(false);
				}
			} else {
				if (isPhotoBackupServiceRunning()) {
                    PhotoBackupService service = PhotoBackupService.getInstance();
                    if (service != null) {
                        Log.i(LOG_TAG, "stop PhotoBackup service");
                        service.stopSelf();
                    }
				}
			}

		} else if (key.equals(PREF_SERVER_URL)) {
			EditTextPreference textPreference = (EditTextPreference) settingsFragment.findPreference(PREF_SERVER_URL);
			textPreference.setSummary(sharedPreferences.getString(PREF_SERVER_URL, ""));

		} else if (key.equals(PREF_SERVER_PASS)) {
			// store only the hash of the password in the preferences
			if (!hashIsComputed) {
				String pass = sharedPreferences.getString(PREF_SERVER_PASS, "");

				try {
					// compute the hash
					MessageDigest md = MessageDigest.getInstance("SHA-512");
					md.update(pass.getBytes());
					byte[] mb = md.digest();
					String hash = "";
					for (byte temp : mb) {
						String s = Integer.toHexString(temp);
						while (s.length() < 2) {
							s = "0" + s;
						}
						s = s.substring(s.length() - 2);
						hash += s;
					}

					// set the hash in the preferences
                    sharedPreferencesEditor.putString(PREF_SERVER_PASS_HASH, hash);
                    sharedPreferencesEditor.commit();

                } catch (NoSuchAlgorithmException e) {
					Log.e(LOG_TAG, "ERROR: " + e.getMessage());
				}
			} else {
				hashIsComputed = false;
			}

            // update fragment
            String serverPassHash = sharedPreferences.getString(PREF_SERVER_PASS_HASH, "");
            EditTextPreference serverPassTextPreference = (EditTextPreference) settingsFragment.findPreference(PREF_SERVER_PASS);
            if (serverPassHash.isEmpty()) {
                serverPassTextPreference.setSummary(getResources().getString(R.string.server_password_summary));
            } else {
                serverPassTextPreference.setSummary(getResources().getString(R.string.server_password_summary_set));
            }

        } else if (key.equals(PREF_SERVER_PASS_HASH)) {
			hashIsComputed = true;

			// Remove the real password from the preferences, for security.
            sharedPreferencesEditor.putString(PREF_SERVER_PASS, "");
            sharedPreferencesEditor.commit();

		} else if (key.equals(PREF_ONLY_WIFI)) {
			// Allow the user not to use the mobile network to upload the pictures
			Toast.makeText(this, "This has no effect for the moment :-)", Toast.LENGTH_SHORT).show();

			// TODO: implement
		}

    }


	private boolean validateSettings() {
		String serverUrl = sharedPreferences.getString(PREF_SERVER_URL, "");
		if (!URLUtil.isValidUrl(serverUrl) || serverUrl.isEmpty()) {
			Toast.makeText(this, R.string.toast_urisyntaxexception, Toast.LENGTH_LONG).show();
            return false;
		}

        String serverPassHash = sharedPreferences.getString(PREF_SERVER_PASS_HASH, "");
        if (serverPassHash.isEmpty()) {
            Toast.makeText(this, R.string.toast_serverpassempty, Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
	}


	// Returns the current state of the PhotoBackup Service
	// See http://stackoverflow.com/a/5921190/417006
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
