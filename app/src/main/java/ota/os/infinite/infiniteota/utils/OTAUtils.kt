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

package ota.os.infinite.infiniteota.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import ota.os.infinite.infiniteota.configs.OTAConfig
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Properties

object OTAUtils {

    private val TAG = "InfiniteOTA"
    private val BUILD_PROP = "/system/build.prop"

    fun logError(e: Exception) {
        Log.e(TAG, e.message, e)
    }

    fun logInfo(message: String) {
        Log.i(TAG, message)
    }

    fun toast(messageId: Int, context: Context?) {
        if (context != null) {
            Toast.makeText(context, context.resources.getString(messageId),
                    Toast.LENGTH_LONG).show()
        }
    }

    fun getDeviceName(context: Context): String {
        val propName = OTAConfig.getInstance(context).deviceSource
        return OTAUtils.getBuildProp(propName)
    }

    fun getBuildProp(propertyName: String): String {
        val buildProps = Properties()
        try {
            val `is` = FileInputStream(File(BUILD_PROP))
            buildProps.load(`is`)
            `is`.close()
            return buildProps.getProperty(propertyName, "")
        } catch (e: IOException) {
            logError(e)
        }

        return ""
    }

    fun runCommand(command: String): String {
        try {
            val output = StringBuffer()
            val p = Runtime.getRuntime().exec(command)
            val reader = BufferedReader(InputStreamReader(p.inputStream))
            var line: String
            while (true) {
                line = reader.readLine() ?: break
                output.append(line + "\n")
            }
            reader.close()
            p.waitFor()
            return output.toString()
        } catch (e: InterruptedException) {
            logError(e)
        } catch (e: IOException) {
            logError(e)
        }

        return ""
    }

    @Throws(IOException::class)
    fun downloadURL(link: String): InputStream {
        val url = URL(link)
        val conn = url.openConnection() as HttpURLConnection
        conn.readTimeout = 10000
        conn.connectTimeout = 15000
        conn.requestMethod = "GET"
        conn.doInput = true
        conn.connect()
        logInfo("downloadStatus: " + conn.responseCode)
        return conn.inputStream
    }

    fun launchUrl(url: String, context: Context?) {
        if (!url.isEmpty() && context != null) {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }
}
