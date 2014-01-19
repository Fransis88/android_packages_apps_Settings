/*
 * Copyright (C) 2013 BeerGang
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

import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.util.Helpers;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class StatusBarStyle extends SettingsPreferenceFragment implements
OnPreferenceChangeListener {
    
    private static final String TAG = "StatusBarStyle";

    private static final String QUICK_PULLDOWN = "quick_pulldown";
    private static final String PRE_COLLAPSE_PANEL = "collapse_panel";

    private static final String PREF_CUSTOM_STATUS_BAR_COLOR = "custom_status_bar_color";
    private static final String PREF_STATUS_BAR_OPAQUE_COLOR = "status_bar_opaque_color";
//    private static final String PREF_STATUS_BAR_SEMI_TRANS_COLOR = "status_bar_trans_color";
    private static final String PREF_CUSTOM_SYSTEM_ICON_COLOR = "custom_system_icon_color";
    private static final String PREF_SYSTEM_ICON_COLOR = "system_icon_color";

    private ListPreference mQuickPulldown;
    CheckBoxPreference mCollapsePanel;

    private CheckBoxPreference mCustomBarColor;
    private ColorPickerPreference mBarOpaqueColor;
//    private ColorPickerPreference mBarTransColor;
    private CheckBoxPreference mCustomIconColor;
    private ColorPickerPreference mIconColor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.xml.status_bar_style);
        
        // Quick Settings pull down
        mQuickPulldown = (ListPreference) getPreferenceScreen().findPreference(QUICK_PULLDOWN);
        mQuickPulldown.setOnPreferenceChangeListener(this);
        int quickPulldownValue = Settings.System.getInt(getActivity().getApplicationContext()
                                                        .getContentResolver(),
                                                        Settings.System.QS_QUICK_PULLDOWN, 0);
        mQuickPulldown.setValue(String.valueOf(quickPulldownValue));
        updatePulldownSummary(quickPulldownValue);

        mCollapsePanel = (CheckBoxPreference) findPreference(PRE_COLLAPSE_PANEL);
        mCollapsePanel.setChecked(Settings.System.getInt(getContentResolver(),
        Settings.System.QS_COLLAPSE_PANEL, 0) == 1);
        mCollapsePanel.setOnPreferenceChangeListener(this);

        int intColor;
        String hexColor;


        PackageManager pm = getPackageManager();
        Resources systemUiResources;
        try {
            systemUiResources = pm.getResourcesForApplication("com.android.systemui");
        } catch (Exception e) {
            Log.e(TAG, "can't access systemui resources",e);
            return;
        }

        mCustomBarColor = (CheckBoxPreference) findPreference(PREF_CUSTOM_STATUS_BAR_COLOR);
        mCustomBarColor.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.CUSTOM_STATUS_BAR_COLOR, 0) == 1);

        mCustomIconColor = (CheckBoxPreference) findPreference(PREF_CUSTOM_SYSTEM_ICON_COLOR);
        mCustomIconColor.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.CUSTOM_SYSTEM_ICON_COLOR, 0) == 1);

        mBarOpaqueColor = (ColorPickerPreference) findPreference(PREF_STATUS_BAR_OPAQUE_COLOR);
        mBarOpaqueColor.setOnPreferenceChangeListener(this);
        intColor = Settings.System.getInt(getContentResolver(),
                    Settings.System.STATUS_BAR_OPAQUE_COLOR, 0xff000000);
        mBarOpaqueColor.setSummary(getResources().getString(R.string.default_string));
        if (intColor == 0xff000000) {
            intColor = systemUiResources.getColor(systemUiResources.getIdentifier(
                    "com.android.systemui:color/system_bar_background_opaque", null, null));
        } else {
            hexColor = String.format("#%08x", (0xffffffff & intColor));
            mBarOpaqueColor.setSummary(hexColor);
        }
        mBarOpaqueColor.setNewPreviewColor(intColor);

//        mBarTransColor = (ColorPickerPreference) findPreference(PREF_STATUS_BAR_SEMI_TRANS_COLOR);
//        mBarTransColor.setOnPreferenceChangeListener(this);
//        intColor = Settings.System.getInt(getActivity().getContentResolver(),
//                    Settings.System.STATUS_BAR_SEMI_TRANS_COLOR, 0x66000000);
//        mBarTransColor.setSummary(getResources().getString(R.string.default_string));
//        if (intColor == 0xff000000) {
//            intColor = systemUiResources.getColor(systemUiResources.getIdentifier(
//                    "com.android.systemui:color/system_bar_background_semi_transparent", null, null));
//        } else {
//            hexColor = String.format("#%08x", (0x66ffffff & intColor));
//            mBarTransColor.setSummary(hexColor);
//        }
//        mBarTransColor.setNewPreviewColor(intColor);

        mIconColor = (ColorPickerPreference) findPreference(PREF_SYSTEM_ICON_COLOR);
        mIconColor.setOnPreferenceChangeListener(this);
        intColor = Settings.System.getInt(getContentResolver(),
                    Settings.System.SYSTEM_ICON_COLOR, -1);
        mIconColor.setSummary(getResources().getString(R.string.default_string));
        if (intColor == 0xffffffff) {
            intColor = systemUiResources.getColor(systemUiResources.getIdentifier(
                    "com.android.systemui:color/status_bar_clock_color", null, null));
        } else {
           hexColor = String.format("#%08x", (0xffffffff & intColor));
            mIconColor.setSummary(hexColor);
        }
        mIconColor.setNewPreviewColor(intColor);
    }

    private void updatePulldownSummary(int value) {
        Resources res = getResources();
        if (value == 0) {
            /* quick pulldown deactivated */
            mQuickPulldown.setSummary(res.getString(R.string.quick_pulldown_off));
        } else {
            String direction = res.getString(value == 2
                                             ? R.string.quick_pulldown_summary_left : R.string.quick_pulldown_summary_right);
            mQuickPulldown.setSummary(res.getString(R.string.quick_pulldown_summary, direction));
        }
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mQuickPulldown) {
            int quickPulldownValue = Integer.valueOf((String) objValue);
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                                   Settings.System.QS_QUICK_PULLDOWN, quickPulldownValue);
            updatePulldownSummary(quickPulldownValue);
            return true;
        } else if (preference == mCollapsePanel) {
            Settings.System.putInt(getContentResolver(), Settings.System.QS_COLLAPSE_PANEL, (Boolean) objValue ? 1 : 0);
            return true;
        } else if (preference == mBarOpaqueColor) {
            String hex = ColorPickerPreference.convertToARGB(Integer
                    .valueOf(String.valueOf(objValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_OPAQUE_COLOR, intHex);
            Helpers.restartSystemUI();
            return true;
        } else if (preference == mIconColor) {
            String hex = ColorPickerPreference.convertToARGB(Integer
                    .valueOf(String.valueOf(objValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.SYSTEM_ICON_COLOR, intHex);
            Helpers.restartSystemUI();
            return true;
//        } else if (preference == mBarTransColor) {
//            String hex = ColorPickerPreference.convertToARGB(Integer
//                    .valueOf(String.valueOf(objValue)));
//            preference.setSummary(hex);
//            int intHex = ColorPickerPreference.convertToColorInt(hex);
//            Settings.System.putInt(getActivity().getContentResolver(),
//                    Settings.System.STATUS_BAR_SEMI_TRANS_COLOR, intHex);
//            Helpers.restartSystemUI();
//            return true;
        }
        return false;
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean value;

        if (preference == mCustomBarColor) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.CUSTOM_STATUS_BAR_COLOR,
            mCustomBarColor.isChecked() ? 1 : 0);
            Helpers.restartSystemUI();
            return true;
        } else if (preference == mCustomIconColor) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.CUSTOM_SYSTEM_ICON_COLOR,
            mCustomIconColor.isChecked() ? 1 : 0);
            Helpers.restartSystemUI();
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}
