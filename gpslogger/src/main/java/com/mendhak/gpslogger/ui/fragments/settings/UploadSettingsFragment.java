/*
 * Copyright (C) 2016 mendhak
 *
 * This file is part of GPSLogger for Android.
 *
 * GPSLogger for Android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * GPSLogger for Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GPSLogger for Android.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mendhak.gpslogger.ui.fragments.settings;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.InputType;
import android.view.View;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.mendhak.gpslogger.MainPreferenceActivity;
import com.mendhak.gpslogger.R;
import com.mendhak.gpslogger.common.PreferenceHelper;
import com.mendhak.gpslogger.common.PreferenceNames;
import com.mendhak.gpslogger.common.Strings;
import com.mendhak.gpslogger.ui.components.SwitchPlusClickPreference;

import eltos.simpledialogfragment.SimpleDialog;
import eltos.simpledialogfragment.form.Input;
import eltos.simpledialogfragment.form.SimpleFormDialog;

/**
 * A {@link android.preference.PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */

public class UploadSettingsFragment
        extends PreferenceFragmentCompat
        implements Preference.OnPreferenceClickListener, SimpleDialog.OnDialogResultListener {

    private static final String PREF_BACKGROUND_STATUS = "autosend_background_status";
    private static final String DIALOG_BACKGROUND_RESTRICTED = "autosend_background_restricted";

    PreferenceHelper preferenceHelper = PreferenceHelper.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Preference bgStatusPref = findPreference(PREF_BACKGROUND_STATUS);
        if (bgStatusPref != null) {
            bgStatusPref.setOnPreferenceClickListener(this);
        }

        Preference autoSendEnabledPref = findPreference(PreferenceNames.AUTOSEND_ENABLED);
        if (autoSendEnabledPref != null) {
            autoSendEnabledPref.setOnPreferenceChangeListener((preference, newValue) -> {
                if (Boolean.TRUE.equals(newValue) && isBackgroundRestricted()) {
                    SimpleDialog.build()
                            .title(R.string.autosend_background_restricted_dialog_title)
                            .msg(R.string.autosend_background_restricted_dialog_msg)
                            .pos(R.string.open_settings)
                            .neg(android.R.string.cancel)
                            .show(this, DIALOG_BACKGROUND_RESTRICTED);
                }
                return true;
            });
        }

        findPreference(PreferenceNames.AUTOSEND_FREQUENCY).setOnPreferenceClickListener(this);
        findPreference(PreferenceNames.AUTOSEND_FREQUENCY).setSummary(preferenceHelper.getAutoSendInterval() + getString(R.string.minutes));

        Preference wifiSSIDsPreference = findPreference(PreferenceNames.AUTOSEND_WIFI_SSIDS);
        if (wifiSSIDsPreference != null) {
            wifiSSIDsPreference.setOnPreferenceClickListener(this);
            updateWifiSSIDsSummary(wifiSSIDsPreference);
        }

        ((SwitchPlusClickPreference)findPreference(PreferenceNames.AUTOSEND_CUSTOMURL_ENABLED))
                .setSwitchClickListener(new SwitchPlusClickPreference.SwitchPlusClickListener() {

                    @Override
                    public void onCheckedChanged(SwitchCompat buttonView, boolean isChecked) {
                        if(isChecked){
                            preferenceHelper.setShouldLogToCSV(true);
                        }
                    }

                    @Override
                    public void onClick(View view) {

                        Intent intent = new Intent(getActivity(), MainPreferenceActivity.class);
                        intent.putExtra("preference_fragment", MainPreferenceActivity.PREFERENCE_FRAGMENTS.CUSTOMURL);
                        startActivity(intent);
                    }
                });

    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.pref_upload, rootKey);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateBackgroundStatusPreference();
    }

    private boolean isBackgroundRestricted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && getContext() != null) {
            ActivityManager am = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
            return am != null && am.isBackgroundRestricted();
        }
        return false;
    }

    private boolean isIgnoringBatteryOptimizations() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && getContext() != null) {
            PowerManager pm = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
            return pm != null && pm.isIgnoringBatteryOptimizations(getContext().getPackageName());
        }
        return true;
    }

    private void updateBackgroundStatusPreference() {
        Preference bgStatusPref = findPreference(PREF_BACKGROUND_STATUS);
        if (bgStatusPref == null) {
            return;
        }
        if (isBackgroundRestricted()) {
            bgStatusPref.setSummary(R.string.autosend_background_status_restricted);
        } else if (isIgnoringBatteryOptimizations()) {
            bgStatusPref.setSummary(R.string.autosend_background_status_unrestricted);
        } else {
            bgStatusPref.setSummary(R.string.autosend_background_status_optimized);
        }
    }

    private void openAppBatterySettings() {
        if (getContext() == null) {
            return;
        }
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", getContext().getPackageName(), null));
        startActivity(intent);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {

        if (preference.getKey().equalsIgnoreCase(PREF_BACKGROUND_STATUS)) {
            openAppBatterySettings();
            return true;
        }

        if(preference.getKey().equalsIgnoreCase(PreferenceNames.AUTOSEND_FREQUENCY)){
            SimpleFormDialog.build().title(R.string.autosend_frequency)
                    .msg(R.string.autosend_frequency_summary)
                    .fields(
                            Input.plain(PreferenceNames.AUTOSEND_FREQUENCY)
                                    .required()
                                    .hint(R.string.autosend_frequency_hint)
                                    .text(String.valueOf(preferenceHelper.getAutoSendInterval()))
                                    .inputType(InputType.TYPE_CLASS_NUMBER)
                    )
                    .show(this, PreferenceNames.AUTOSEND_FREQUENCY);
            return true;
        }

        if(preference.getKey().equalsIgnoreCase(PreferenceNames.AUTOSEND_WIFI_SSIDS)){
            String currentSSID = com.mendhak.gpslogger.common.network.Networks.getCurrentWifiSSID(getActivity());
            String msg = getString(R.string.autosend_wifi_ssids_summary);
            if (!com.mendhak.gpslogger.common.Strings.isNullOrEmpty(currentSSID)) {
                msg += "\n\n" + String.format(getString(R.string.autosend_wifi_ssids_current_wifi), currentSSID);
            }

            SimpleFormDialog.build()
                    .title(R.string.autosend_wifi_ssids_title)
                    .msg(msg)
                    .fields(
                            Input.plain(PreferenceNames.AUTOSEND_WIFI_SSIDS)
                                    .hint(R.string.autosend_wifi_ssids_hint)
                                    .text(preferenceHelper.getAutoSendWifiSSIDs())
                    )
                    .show(this, PreferenceNames.AUTOSEND_WIFI_SSIDS);
            return true;
        }

        return false;
    }

    private void updateWifiSSIDsSummary(Preference preference) {
        String ssids = preferenceHelper.getAutoSendWifiSSIDs();
        if (com.mendhak.gpslogger.common.Strings.isNullOrEmpty(ssids)) {
            preference.setSummary(R.string.autosend_wifi_ssids_summary);
        } else {
            preference.setSummary(ssids);
        }
    }

    @Override
    public boolean onResult(@NonNull String dialogTag, int which, @NonNull Bundle extras) {
        if(dialogTag.equalsIgnoreCase(PreferenceNames.AUTOSEND_FREQUENCY) && which == BUTTON_POSITIVE){
            String freq = extras.getString(PreferenceNames.AUTOSEND_FREQUENCY);
            preferenceHelper.setAutoSendInterval(freq);
            findPreference(PreferenceNames.AUTOSEND_FREQUENCY).setSummary(freq + getString(R.string.minutes));
            return true;
        }

        if(dialogTag.equalsIgnoreCase(PreferenceNames.AUTOSEND_WIFI_SSIDS) && which == BUTTON_POSITIVE){
            String ssids = extras.getString(PreferenceNames.AUTOSEND_WIFI_SSIDS);
            preferenceHelper.setAutoSendWifiSSIDs(ssids);
            Preference pref = findPreference(PreferenceNames.AUTOSEND_WIFI_SSIDS);
            if (pref != null) {
                updateWifiSSIDsSummary(pref);
            }
            return true;
        }
        if(dialogTag.equalsIgnoreCase(DIALOG_BACKGROUND_RESTRICTED) && which == BUTTON_POSITIVE){
            openAppBatterySettings();
            return true;
        }
        return false;
    }
}
