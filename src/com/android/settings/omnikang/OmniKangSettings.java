/*
 * Copyright (C) 2013 OmniKang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.omnikang;

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.provider.Settings;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.PreferenceCategory;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.WindowManagerGlobal;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class OmniKangSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "OmniKangSettings";

    private static final String SOFT_BACK_KILL_APP = "soft_back_kill_app";
    private static final String EMULATE_MENU_KEY = "emulate_menu_key";

    private static final String CATEGORY_NAVBAR = "navigation_bar";

    private CheckBoxPreference mSoftBackKillApp;
    private CheckBoxPreference mEmulateMenuKey;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.xml.omnikang_settings);
        PreferenceScreen prefSet = getPreferenceScreen();

        ContentResolver resolver = getActivity().getContentResolver();

        boolean hasNavBar = getResources().getBoolean(
                com.android.internal.R.bool.config_showNavigationBar);
        // Also check, if users without navigation bar force enabled it.
        hasNavBar = hasNavBar || (SystemProperties.getInt("qemu.hw.mainkeys", 1) == 0);

        // Hide navigation bar category on devices without navigation bar
        if (!hasNavBar) {
            prefSet.removePreference(findPreference(CATEGORY_NAVBAR));
        } else {
            mSoftBackKillApp = (CheckBoxPreference) prefSet.findPreference(SOFT_BACK_KILL_APP);
            mSoftBackKillApp.setChecked(Settings.System.getInt(resolver,
                    Settings.System.SOFT_BACK_KILL_APP_ENABLE, 0) == 1);
            mSoftBackKillApp.setOnPreferenceChangeListener(this);

            mEmulateMenuKey = (CheckBoxPreference) prefSet.findPreference(EMULATE_MENU_KEY);
            mEmulateMenuKey.setChecked(Settings.System.getInt(resolver,
                    Settings.System.EMULATE_HW_MENU_KEY, 0) == 1);
            mEmulateMenuKey.setOnPreferenceChangeListener(this);

        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
    
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();

        if (preference == mSoftBackKillApp) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(resolver,
                    Settings.System.SOFT_BACK_KILL_APP_ENABLE, value ? 1 : 0);
        } else if (preference == mEmulateMenuKey) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(resolver,
                    Settings.System.EMULATE_HW_MENU_KEY, value ? 1 : 0);
        } else {
            return false;
        }
        return true;
    }


    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        ContentResolver resolver = getActivity().getContentResolver();
        boolean value;

//        if (preference == mDummyPref) {
//            Settings.System.putInt(resolver, Settings.System.DUMMY_PREF,
//                    mDummyPref.isChecked() ? 1 : 0);
//        } else {
            // If we didn't handle it, let preferences handle it.
            return super.onPreferenceTreeClick(preferenceScreen, preference);
//        }
        return true;
    }
}
