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

package ota.os.infinite.infiniteota.configs

import android.app.AlarmManager
import android.content.Context
import android.preference.PreferenceManager

import com.commonsware.cwac.wakeful.WakefulIntentService
import ota.os.infinite.infiniteota.R
import ota.os.infinite.infiniteota.scheduler.OTAListener
import ota.os.infinite.infiniteota.utils.OTAUtils

import java.text.DateFormat
import java.util.Date

object AppConfig {

    val lastCheckKey = "last_check"
    val updateIntervalKey = "update_interval"
    val latestVersionKey = "latest_version"

    private fun buildLastCheckSummary(time: Long, context: Context): String {
        val prefix = context.resources.getString(R.string.last_check_summary)
        if (time > 0) {
            val date = DateFormat.getDateTimeInstance().format(Date(time))
            return String.format(prefix, date)
        }
        return String.format(prefix, context.resources.getString(R.string.last_check_never))
    }

    fun getLastCheck(context: Context): String {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val time = sharedPreferences.getLong(lastCheckKey, 0)
        return buildLastCheckSummary(time, context)
    }

    fun getFullLatestVersion(context: Context): String {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        return sharedPreferences.getString(latestVersionKey, "")
    }

    fun persistLatestVersion(latestVersion: String, context: Context) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        sharedPreferences.edit().putString(latestVersionKey, latestVersion).apply()
    }

    fun persistLastCheck(context: Context) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        sharedPreferences.edit().putLong(lastCheckKey, System.currentTimeMillis()).apply()
    }

    fun persistUpdateIntervalIndex(intervalIndex: Int, context: Context) {
        val intervalValue: Long = when (intervalIndex) {
            0 -> 0
            1 -> AlarmManager.INTERVAL_HOUR
            2 -> AlarmManager.INTERVAL_HALF_DAY
            3 -> AlarmManager.INTERVAL_DAY
            else -> OTAListener.DEFAULT_INTERVAL_VALUE
        }

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        sharedPreferences.edit().putLong(updateIntervalKey, intervalValue).apply()
        if (intervalValue > 0) {
            WakefulIntentService.cancelAlarms(context)
            WakefulIntentService.scheduleAlarms(OTAListener(), context, true)
            OTAUtils.toast(R.string.autoupdate_enabled, context)
        } else {
            WakefulIntentService.cancelAlarms(context)
            OTAUtils.toast(R.string.autoupdate_disabled, context)
        }
    }

    fun getUpdateIntervalIndex(context: Context): Int {
        val value = getUpdateIntervalTime(context)
        val index: Int
        index = when (value) {
            0L -> 0
            AlarmManager.INTERVAL_HOUR -> 1
            AlarmManager.INTERVAL_HALF_DAY -> 2
            else -> 3
        }
        return index
    }

    fun getUpdateIntervalTime(context: Context): Long {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        return sharedPreferences.getLong(updateIntervalKey, OTAListener.DEFAULT_INTERVAL_VALUE)
    }
}
