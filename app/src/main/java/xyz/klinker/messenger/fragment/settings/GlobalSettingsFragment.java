/*
 * Copyright (C) 2016 Jacob Klinker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xyz.klinker.messenger.fragment.settings;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import xyz.klinker.messenger.R;
import xyz.klinker.messenger.api.implementation.Account;
import xyz.klinker.messenger.api.implementation.ApiUtils;
import xyz.klinker.messenger.data.ColorSet;
import xyz.klinker.messenger.data.FeatureFlags;
import xyz.klinker.messenger.data.Settings;
import xyz.klinker.messenger.util.ColorUtils;
import xyz.klinker.messenger.view.NotificationAlertsPreference;

/**
 * Fragment for modifying app settings_global.
 */
public class GlobalSettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings_global);
        initBaseTheme();
        initGlobalTheme();
        initFontSize();
        initDeliveryReports();
        initConvertToMMS();
        initSignature();
        initSoundEffects();
    }

    @Override
    public void onStop() {
        super.onStop();
        Settings.get(getActivity()).forceUpdate();
    }

    private void initGlobalTheme() {
        findPreference(getString(R.string.pref_global_color_theme))
                .setOnPreferenceChangeListener((preference, o) -> {
                    ColorSet initialColors = ColorSet.getFromString(getActivity(),
                            Settings.get(getActivity()).themeColorString);
                    String colorString = (String) o;
                    new ApiUtils().updateGlobalThemeColor(Account.get(getActivity()).accountId,
                            colorString);

                    ColorSet colors = ColorSet.getFromString(getActivity(), colorString);
                    ColorUtils.animateToolbarColor(getActivity(),
                            initialColors.color, colors.color);
                    ColorUtils.animateStatusBarColor(getActivity(),
                            initialColors.colorDark, colors.colorDark);

                    return true;
                });
    }

    private void initBaseTheme() {
        findPreference(getString(R.string.pref_base_theme))
                .setOnPreferenceChangeListener((preference, o) -> {
                    String newValue = (String) o;
                    if (!newValue.equals("day_night") && !newValue.equals("light")) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    } else {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    }

                    new ApiUtils().updateBaseTheme(Account.get(getActivity()).accountId,
                            newValue);

                    getActivity().recreate();

                    return true;
                });
    }

    private void initFontSize() {
        findPreference(getString(R.string.pref_font_size))
                .setOnPreferenceChangeListener((preference, o) -> {
                    String size = (String) o;
                    new ApiUtils().updateFontSize(Account.get(getActivity()).accountId,
                            size);

                    return true;
                });
    }

    private void initDeliveryReports() {
        findPreference(getString(R.string.pref_delivery_reports))
                .setOnPreferenceChangeListener((preference, o) -> {
                    boolean delivery = (boolean) o;
                    new ApiUtils().updateDeliveryReports(Account.get(getActivity()).accountId,
                            delivery);
                    return true;
                });
    }

    private void initConvertToMMS() {
        findPreference(getString(R.string.pref_convert_to_mms))
                .setOnPreferenceChangeListener((preference, o) -> {
                    boolean convert = (boolean) o;
                    new ApiUtils().updateConvertToMMS(Account.get(getActivity()).accountId,
                            convert);
                    return true;
                });
    }

    private void initSignature() {
        Preference preference = findPreference(getString(R.string.pref_signature));
        preference.setOnPreferenceClickListener(p -> {
                    //noinspection AndroidLintInflateParams
                    View layout = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_edit_text,
                            null, false);
                    final EditText editText = (EditText) layout.findViewById(R.id.edit_text);
                    editText.setHint(R.string.signature);
                    editText.setText(Settings.get(getActivity()).signature);
                    editText.setSelection(editText.getText().length());

                    new AlertDialog.Builder(getActivity())
                            .setView(layout)
                            .setPositiveButton(R.string.save, (dialogInterface, i) -> {
                                if (editText.getText().length() > 0) {
                                    new ApiUtils().updateSignature(Account.get(getActivity()).accountId,
                                            editText.getText().toString());
                                } else {
                                    new ApiUtils().updateSignature(Account.get(getActivity()).accountId, "");
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, null)
                            .show();

                    return false;
                });

        if (FeatureFlags.get(getActivity()).FEATURE_SETTINGS) {
            PreferenceGroup group = (PreferenceGroup) getPreferenceScreen()
                    .findPreference(getString(R.string.pref_advanced_category));
            group.removePreference(preference);
        }
    }

    private void initSoundEffects() {
        findPreference(getString(R.string.pref_sound_effects))
                .setOnPreferenceChangeListener((preference, o) -> {
                    boolean effects = (boolean) o;
                    new ApiUtils().updateSoundEffects(Account.get(getActivity()).accountId,
                            effects);
                    return true;
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ((NotificationAlertsPreference) findPreference(getString(R.string.pref_alert_types)))
                .handleRingtoneResult(requestCode, resultCode, data);
    }

}