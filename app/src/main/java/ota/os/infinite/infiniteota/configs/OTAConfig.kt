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

import android.content.Context
import ota.os.infinite.infiniteota.utils.OTAUtils

import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Properties

class OTAConfig private constructor() : Properties() {

    val otaUrl: String
        get() = getProperty(OTAConfig.OTA_URL, "")

    val releaseType: String
        get() = getProperty(OTAConfig.RELEASE_TYPE, "Stable")

    val versionSource: String
        get() = getProperty(VERSION_SOURCE, getProperty("version_name", ""))

    val deviceSource: String
        get() = getProperty(OTAConfig.DEVICE_NAME, "")

    val delimiter: String
        get() = getProperty(OTAConfig.VERSION_DELIMITER, "")

    val position: Int
        get() {

            return try {
                Integer.parseInt(getProperty(VERSION_POSITION))
            } catch (e: NumberFormatException) {
                -1
            }
        }

    val format: SimpleDateFormat?
        get() {
            val format = getProperty(OTAConfig.VERSION_FORMAT, "")
            if (format.isEmpty()) {
                return null
            }

            try {
                return SimpleDateFormat(format, Locale.US)
            } catch (e: IllegalArgumentException) {
                OTAUtils.logError(e)
            } catch (e: NullPointerException) {
                OTAUtils.logError(e)
            }

            return null
        }

    companion object {

        private val FILENAME = "ota_conf"

        private val OTA_URL = "ota_url"
        private val RELEASE_TYPE = "release_type"

        private val DEVICE_NAME = "device_name"

        private val VERSION_SOURCE = "version_source"
        private val VERSION_DELIMITER = "version_delimiter"
        private val VERSION_FORMAT = "version_format"
        private val VERSION_POSITION = "version_position"

        private var mInstance: OTAConfig? = null

        fun getInstance(context: Context): OTAConfig {
            if (mInstance == null) {
                mInstance = OTAConfig()
                try {
                    val `is` = context.assets.open(FILENAME)
                    mInstance!!.load(`is`)
                    `is`.close()
                } catch (e: IOException) {
                    OTAUtils.logError(e)
                }

            }
            return mInstance as OTAConfig
        }
    }
}
