/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.messaging.ui.appsettings;

import android.app.FragmentTransaction;
import android.app.role.RoleManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;
import android.provider.Settings;
import androidx.core.app.NavUtils;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.android.messaging.R;
import com.android.messaging.ui.BugleActionBarActivity;
import com.android.messaging.ui.LicenseActivity;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.util.BuglePrefs;
import com.android.messaging.util.DebugUtils;
import com.android.messaging.util.OsUtil;
import com.android.messaging.util.PhoneUtils;

public class ApplicationSettingsActivity extends BugleActionBarActivity {
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final boolean topLevel = getIntent().getBooleanExtra(
                UIIntents.UI_INTENT_EXTRA_TOP_LEVEL_SETTINGS, false);
        if (topLevel) {
            getSupportActionBar().setTitle(getString(R.string.settings_activity_title));
        }
        getWindow().getDecorView().setBackgroundColor(getColor(R.color.contextual_action_bar_background_color));
        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, new ApplicationSettingsFragment()).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (super.onCreateOptionsMenu(menu)) {
            return true;
        }
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            NavUtils.navigateUpFromSameTask(this);
            return true;
        case R.id.action_license:
            final Intent intent = new Intent(this, LicenseActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class ApplicationSettingsFragment extends PreferenceFragmentCompat {

        private String mNotificationsPreferenceKey;
        private Preference mNotificationsPreference;
        private String mSmsEnabledPrefKey;
        private Preference mSmsEnabledPreference;
        private String mSwipeRightToDeleteConversationkey;
        private SwitchPreference mSwipeRightToDeleteConversationPreference;

        public ApplicationSettingsFragment() {
            // Required empty constructor
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

            getPreferenceManager().setSharedPreferencesName(BuglePrefs.SHARED_PREFERENCES_NAME);
            addPreferencesFromResource(R.xml.preferences_application);

            mNotificationsPreferenceKey =
                    getString(R.string.notifications_pref_key);
            mNotificationsPreference = findPreference(mNotificationsPreferenceKey);
            mSmsEnabledPrefKey = getString(R.string.sms_enabled_pref_key);
            mSmsEnabledPreference = findPreference(mSmsEnabledPrefKey);
            mSwipeRightToDeleteConversationkey = getString(
                    R.string.swipe_right_deletes_conversation_key);
            mSwipeRightToDeleteConversationPreference =
                    (SwitchPreference) findPreference(mSwipeRightToDeleteConversationkey);

            if (!DebugUtils.isDebugEnabled()) {
                final Preference debugCategory = findPreference(getString(
                        R.string.debug_pref_key));
                getPreferenceScreen().removePreference(debugCategory);
            }

            final PreferenceScreen advancedScreen = (PreferenceScreen) findPreference(
                    getString(R.string.advanced_pref_key));
            final boolean topLevel = getActivity().getIntent().getBooleanExtra(
                    UIIntents.UI_INTENT_EXTRA_TOP_LEVEL_SETTINGS, false);
            if (topLevel) {
                advancedScreen.setIntent(UIIntents.get()
                        .getAdvancedSettingsIntent(getPreferenceScreen().getContext()));
            } else {
                // Hide the Advanced settings screen if this is not top-level; these are shown at
                // the parent SettingsActivity.
                getPreferenceScreen().removePreference(advancedScreen);
            }
        }

        @Override
        public boolean onPreferenceTreeClick(Preference preference) {
            if (preference.getKey() == mNotificationsPreferenceKey) {
                Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, getContext().getPackageName());
                startActivity(intent);
                return true;
            } if (preference.getKey() == mSmsEnabledPrefKey) {
                Intent intent = new Intent("android.intent.action.MANAGE_DEFAULT_APP");
                intent.putExtra("android.intent.extra.ROLE_NAME", RoleManager.ROLE_SMS);
                startActivity(intent);
                return true;
            }
            return super.onPreferenceTreeClick(preference);
        }

        private void updateSmsEnabledPreferences() {
            if (!OsUtil.isAtLeastKLP()) {
                getPreferenceScreen().removePreference(mSmsEnabledPreference);
            } else {
                final String defaultSmsAppLabel = getString(R.string.default_sms_app,
                        PhoneUtils.getDefault().getDefaultSmsAppLabel());
                boolean isSmsEnabledCurrentState = PhoneUtils.getDefault().isDefaultSmsApp();
                mSmsEnabledPreference.setSummary(isSmsEnabledCurrentState ? getString(R.string.value_yes) : getString(R.string.value_no));
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            updateSmsEnabledPreferences();
        }
    }
}
