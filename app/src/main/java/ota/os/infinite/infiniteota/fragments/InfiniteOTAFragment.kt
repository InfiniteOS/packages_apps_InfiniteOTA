/*
 * Copyright (C) 2015 Chandra Poerwanto
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

package ota.os.infinite.infiniteota.fragments

import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.preference.ListPreference
import android.preference.Preference
import android.preference.PreferenceCategory
import android.preference.PreferenceFragment
import android.preference.PreferenceScreen
import ota.os.infinite.infiniteota.R
import ota.os.infinite.infiniteota.configs.AppConfig
import ota.os.infinite.infiniteota.configs.LinkConfig
import ota.os.infinite.infiniteota.configs.OTAVersion
import ota.os.infinite.infiniteota.dialogs.WaitDialogFragment
import ota.os.infinite.infiniteota.tasks.CheckUpdateTask
import ota.os.infinite.infiniteota.utils.OTAUtils


class InfiniteOTAFragment : PreferenceFragment(), Preference.OnPreferenceChangeListener, SharedPreferences.OnSharedPreferenceChangeListener, WaitDialogFragment.OTADialogListener, LinkConfig.LinkConfigListener {

    private var mRomInfo: PreferenceScreen? = null
    private var mCheckUpdate: PreferenceScreen? = null
    private var mUpdateInterval: ListPreference? = null
    private var mLinksCategory: PreferenceCategory? = null

    private var mTask: CheckUpdateTask? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true

        addPreferencesFromResource(R.xml.infiniteota)

        mRomInfo = preferenceScreen.findPreference(KEY_ROM_INFO) as PreferenceScreen
        mCheckUpdate = preferenceScreen.findPreference(KEY_CHECK_UPDATE) as PreferenceScreen

        mUpdateInterval = preferenceScreen.findPreference(KEY_UPDATE_INTERVAL) as ListPreference
        if (mUpdateInterval != null) {
            mUpdateInterval!!.onPreferenceChangeListener = this
        }

        mLinksCategory = preferenceScreen.findPreference(CATEGORY_LINKS) as PreferenceCategory
    }

    private fun updatePreferences() {
        updateRomInfo()
        updateLastCheckSummary()
        updateIntervalSummary()
        updateLinks(false)
    }

    private fun updateLinks(force: Boolean) {
        val links = LinkConfig.instance.getLinks(activity, force)
        if (links != null) {
            for (link in links) {
                val id = link.id
                var linkPref: PreferenceScreen? = preferenceScreen.findPreference(id) as PreferenceScreen
                if (linkPref == null && mLinksCategory != null) {
                    linkPref = preferenceManager.createPreferenceScreen(activity)
                    linkPref!!.key = id
                    mLinksCategory!!.addPreference(linkPref)
                }
                if (linkPref != null) {
                    val title = link.title
                    if (title != null) {
                        linkPref.title = if (title.isEmpty()) id else title
                    }
                    linkPref.summary=(link.description)
                }
            }
        }
    }

    private fun updateRomInfo() {
        if (mRomInfo != null) {
            val fullLocalVersion = OTAVersion.getFullLocalVersion(activity)
            val shortLocalVersion = OTAVersion.extractVersionFrom(fullLocalVersion, activity)
            mRomInfo!!.title = fullLocalVersion

            val prefix = activity.resources.getString(R.string.latest_version)
            var fullLatestVersion = AppConfig.getFullLatestVersion(activity)
            val shortLatestVersion = OTAVersion.extractVersionFrom(fullLatestVersion, activity)
            if (fullLatestVersion.isEmpty()) {
                fullLatestVersion = activity.resources.getString(R.string.unknown)
                mRomInfo!!.summary = String.format(prefix, fullLatestVersion)
            } else if (!OTAVersion.compareVersion(shortLatestVersion, shortLocalVersion, activity)) {
                mRomInfo!!.summary = activity.resources.getString(R.string.system_uptodate)
            } else {
                mRomInfo!!.summary = String.format(prefix, fullLatestVersion)
            }
        }
    }

    private fun updateLastCheckSummary() {
        if (mCheckUpdate != null) {
            mCheckUpdate!!.summary = AppConfig.getLastCheck(activity)
        }
    }

    private fun updateIntervalSummary() {
        if (mUpdateInterval != null) {
            mUpdateInterval!!.setValueIndex(AppConfig.getUpdateIntervalIndex(activity))
            mUpdateInterval!!.summary = mUpdateInterval!!.entry
        }
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        updatePreferences()
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onProgressCancelled() {
        if (mTask != null) {
            mTask!!.cancel(true)
            mTask = null
        }
    }

    override fun onConfigChange() {
        updateLinks(true)
    }

    override fun onPreferenceTreeClick(preferenceScreen: PreferenceScreen, preference: Preference): Boolean {
        val key = preference.key
        when (key) {
            KEY_CHECK_UPDATE -> {
                mTask = CheckUpdateTask.getInstance(false)
                if (mTask!!.status != AsyncTask.Status.RUNNING) {
                    mTask!!.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, activity)
                }
                return true
            }
            else -> {
                val link = LinkConfig.instance.findLink(key, activity)
                if (link != null) {
                    OTAUtils.launchUrl(link.url!!, activity)
                }
            }
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference)
    }

    override fun onPreferenceChange(preference: Preference, value: Any): Boolean {
        if (preference === mUpdateInterval) {
            AppConfig.persistUpdateIntervalIndex(Integer.valueOf(value as String), activity)
            return true
        }
        return false
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key == AppConfig.latestVersionKey) {
            updateRomInfo()
        }
        if (key == AppConfig.lastCheckKey) {
            updateLastCheckSummary()
        }
        if (key == AppConfig.updateIntervalKey) {
            updateIntervalSummary()
        }
    }

    companion object {

        private val KEY_ROM_INFO = "key_rom_info"
        private val KEY_CHECK_UPDATE = "key_check_update"
        private val KEY_UPDATE_INTERVAL = "key_update_interval"
        private val CATEGORY_LINKS = "category_links"
    }
}
