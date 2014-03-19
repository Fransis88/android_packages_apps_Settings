/*
 * Copyright (C) 2013 Slimroms
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

package com.android.settings.slim.themes;

import android.app.ActionBar;
import android.app.Activity;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.ListPreference;
import android.preference.SwitchPreference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class ThemeSettings extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String THEME_AUTO_MODE =
        "pref_theme_auto_mode";

    private static final String KEY_TRDS_ENABLED = "trds_enabled";

    private SwitchPreference mTrdsEnabled;
    private ListPreference mThemeAutoMode;

    private int mCurrentState = 0;
    private boolean mEnabled;

    private boolean mAttached;
    private SettingsObserver mSettingsObserver;

    private final class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            mContext.getContentResolver().registerContentObserver(
                    Settings.Secure.getUriFor(
                    Settings.Secure.UI_THEME_AUTO_MODE),
                    false, this);
        }

        @Override
        public void onChange(boolean selfChange) {
            setSwitchState();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.theme_settings);

        PreferenceScreen prefSet = getPreferenceScreen();

        mSettingsObserver = new SettingsObserver(new Handler());

        mThemeAutoMode = (ListPreference) prefSet.findPreference(THEME_AUTO_MODE);
        mThemeAutoMode.setValue(String.valueOf(
                Settings.Secure.getIntForUser(getContentResolver(),
                Settings.Secure.UI_THEME_AUTO_MODE, 0,
                UserHandle.USER_CURRENT)));
        mThemeAutoMode.setSummary(mThemeAutoMode.getEntry());

        mThemeAutoMode.setOnPreferenceChangeListener(
            new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference,
                        Object newValue) {
                String val = (String) newValue;
                Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.UI_THEME_AUTO_MODE,
                    Integer.valueOf(val));
                int index = mThemeAutoMode.findIndexOfValue(val);
                mThemeAutoMode.setSummary(
                    mThemeAutoMode.getEntries()[index]);
                return true;
            }
        });

        mEnabled = Settings.Secure.getIntForUser(getContentResolver(),
                Settings.Secure.UI_THEME_AUTO_MODE, 0,
                UserHandle.USER_CURRENT) != 1;

        boolean state = getResources().getConfiguration().uiThemeMode
                    == Configuration.UI_THEME_MODE_HOLO_DARK;

        mTrdsEnabled = (SwitchPreference) findPreference(KEY_TRDS_ENABLED);
        mTrdsEnabled.setChecked(state);
        mTrdsEnabled.setOnPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mAttached) {
            mAttached = true;
            mSettingsObserver.observe();
        }
        setSwitchState();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mAttached) {
            mAttached = false;
            mContext.getContentResolver().unregisterContentObserver(mSettingsObserver);
        }
    }

    public void setSwitchState() {
        mEnabled = Settings.Secure.getIntForUser(mContext.getContentResolver(),
                Settings.Secure.UI_THEME_AUTO_MODE, 0,
                UserHandle.USER_CURRENT) != 1;

        boolean state = mContext.getResources().getConfiguration().uiThemeMode
                    == Configuration.UI_THEME_MODE_HOLO_DARK;

        mTrdsEnabled.setChecked(state);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.uiThemeMode != mCurrentState) {
            mCurrentState = newConfig.uiThemeMode;
            setSwitchState();
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mTrdsEnabled) {
            if (!mEnabled) {
                Toast.makeText(mContext, R.string.theme_auto_switch_mode_error,
                        Toast.LENGTH_SHORT).show();
                setSwitchState();
            } else {
                // Handle a switch change
                // we currently switch between holodark and hololight till either
                // theme engine is ready or lightheme is ready. Currently due of
                // missing light themeing hololight = system base theme
                Settings.Secure.putIntForUser(mContext.getContentResolver(),
                        Settings.Secure.UI_THEME_MODE, ((Boolean) newValue).booleanValue()
                            ? Configuration.UI_THEME_MODE_HOLO_DARK
                            : Configuration.UI_THEME_MODE_HOLO_LIGHT,
                        UserHandle.USER_CURRENT);
                setSwitchState();
                return true;
            }
        }
        return false;
    }

}
