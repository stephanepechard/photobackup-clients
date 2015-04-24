/**
 * Copyright (C) 2014 Stéphane Péchard.
 *
 * This file is part of PhotoBackup.
 *
 * Photo Backup is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Photo Backup is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.s13d.photobackup;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.webkit.URLUtil;
import android.widget.Toast;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class PBSettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String LOG_TAG = "PBSettingsFragment";
    private SharedPreferences defaultPreferences;
    private SharedPreferences.Editor defaultSharedPreferences;
    private Boolean hashIsComputed = false;

    // should correspond to what is in preferences.xml
    private static final String PREF_SERVICE_RUNNING = "PREF_SERVICE_RUNNING";
    public static final String PREF_SERVER_URL = "PREF_SERVER_URL";
    private static final String PREF_SERVER_PASS = "PREF_SERVER_PASS";
    public static final String PREF_SERVER_PASS_HASH = "PREF_SERVER_PASS_HASH";
    private static final String PREF_ONLY_WIFI = "PREF_ONLY_WIFI";


    @Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
        defaultPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        defaultSharedPreferences = defaultPreferences.edit();
        fillTextPreferences();

        // Hide upload journal access if it is empty
        int nbPicture = PBActivity.mediaStore.getMediaCount();
        Log.i(LOG_TAG, "Found " + nbPicture + " picture(s)");
        if (nbPicture == 0) {
            final PreferenceScreen preferenceScreen = (PreferenceScreen) findPreference("PBPreferences");
            final PreferenceCategory preferenceCategory = (PreferenceCategory) findPreference("info_conf");
            preferenceScreen.removePreference(preferenceCategory);
        } else {
            final Preference pref = findPreference("uploadJournalPref");
            pref.setTitle(pref.getTitle() + " (" + nbPicture + ")");
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        if (defaultPreferences != null) {
            defaultPreferences.registerOnSharedPreferenceChangeListener(this);
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        if (defaultPreferences != null) {
            defaultPreferences.unregisterOnSharedPreferenceChangeListener(this);
        }
    }


    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {

        if (key.equals(PREF_SERVICE_RUNNING)) {
            Log.i(LOG_TAG, "PREF_SERVICE_RUNNING = " + sharedPreferences.getBoolean(PREF_SERVICE_RUNNING, false));
            this.startOrStopService(sharedPreferences);

        } else if (key.equals(PREF_SERVER_URL)) {
            fillTextPreferences();
        } else if (key.equals(PREF_SERVER_PASS)) {
            this.createAndSetServerPass(sharedPreferences);

            // update fragment
            final String serverPassHash = sharedPreferences.getString(PREF_SERVER_PASS_HASH, "");
            final EditTextPreference serverPassTextPreference = (EditTextPreference) findPreference(PREF_SERVER_PASS);
            if (serverPassHash.isEmpty()) {
                serverPassTextPreference.setSummary(getResources().getString(R.string.server_password_summary));
            } else {
                serverPassTextPreference.setSummary(getResources().getString(R.string.server_password_summary_set));
            }

        } else if (key.equals(PREF_SERVER_PASS_HASH)) {
            hashIsComputed = true;

            // Remove the real password from the preferences, for security.
            defaultSharedPreferences.putString(PREF_SERVER_PASS, "");
            defaultSharedPreferences.commit();

        } else if (key.equals(PREF_ONLY_WIFI)) {
            // Allow the user not to use the mobile network to upload the pictures
            Toast.makeText(getActivity(), "This has no effect for the moment :-)", Toast.LENGTH_SHORT).show();
            // TODO: implement
        } else if (sharedPreferences == null) {
            Log.e(LOG_TAG, "Error: sharedPreferences == null");
        }

    }


    private void startOrStopService(final SharedPreferences sharedPreferences) {
        if (sharedPreferences.getBoolean(PREF_SERVICE_RUNNING, false)) {
            if (validateSettings()) {
                final Intent serviceIntent = new Intent(getActivity(), PBService.class);
                getActivity().startService(serviceIntent);
            } else {
                final SwitchPreference switchPreference = (SwitchPreference) findPreference(PREF_SERVICE_RUNNING);
                switchPreference.setChecked(false);
            }
        } else {
            if (isPhotoBackupServiceRunning()) {
                PBService service = PBService.getInstance();
                if (service != null) {
                    Log.i(LOG_TAG, "stop PhotoBackup service");
                    service.stopSelf();
                }
            }
        }
    }


    private boolean validateSettings() {
        String serverUrl = defaultPreferences.getString(PREF_SERVER_URL, "");
        if (!URLUtil.isValidUrl(serverUrl) || serverUrl.isEmpty()) {
            Toast.makeText(getActivity(), R.string.toast_urisyntaxexception, Toast.LENGTH_LONG).show();
            return false;
        }

        String serverPassHash = defaultPreferences.getString(PREF_SERVER_PASS_HASH, "");
        if (serverPassHash.isEmpty()) {
            Toast.makeText(getActivity(), R.string.toast_serverpassempty, Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }


    // Returns the current state of the PhotoBackup Service
    // See http://stackoverflow.com/a/5921190/417006
    private boolean isPhotoBackupServiceRunning() {
        final ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (PBService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    private void createAndSetServerPass(final SharedPreferences sharedPreferences) {
        // store only the hash of the password in the preferences
        if (!hashIsComputed) {
            final String pass = sharedPreferences.getString(PREF_SERVER_PASS, "");

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
                defaultSharedPreferences.putString(PREF_SERVER_PASS_HASH, hash);
                defaultSharedPreferences.commit();

            } catch (NoSuchAlgorithmException e) {
                Log.e(LOG_TAG, "ERROR: " + e.getMessage());
            }
        } else {
            hashIsComputed = false;
        }
    }


    private void fillTextPreferences() {
        final EditTextPreference textPreference = (EditTextPreference) findPreference(PREF_SERVER_URL);
        textPreference.setSummary(defaultPreferences.getString(PREF_SERVER_URL, this.getResources().getString(R.string.server_url_summary)));
    }
}
