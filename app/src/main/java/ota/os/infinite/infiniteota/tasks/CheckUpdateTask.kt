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

package ota.os.infinite.infiniteota.tasks

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.os.AsyncTask

import org.xmlpull.v1.XmlPullParserException
import ota.os.infinite.infiniteota.MainActivity
import ota.os.infinite.infiniteota.R
import ota.os.infinite.infiniteota.configs.AppConfig
import ota.os.infinite.infiniteota.configs.LinkConfig
import ota.os.infinite.infiniteota.configs.OTAConfig
import ota.os.infinite.infiniteota.configs.OTAVersion
import ota.os.infinite.infiniteota.dialogs.WaitDialogHandler
import ota.os.infinite.infiniteota.utils.OTAUtils
import ota.os.infinite.infiniteota.xml.OTADevice
import ota.os.infinite.infiniteota.xml.OTAParser

import java.io.IOException

@Suppress("DEPRECATION")
class CheckUpdateTask private constructor(private val mIsBackgroundThread: Boolean) : AsyncTask<Context, Void, OTADevice>() {
    private val mHandler = WaitDialogHandler()
    @SuppressLint("StaticFieldLeak")
    private var mContext: Context? = null

    override fun doInBackground(vararg params: Context): OTADevice? {
        mContext = params[0]

        if (!isConnectivityAvailable(mContext)) {
            return null
        }

        showWaitDialog()

        var device: OTADevice? = null
        val deviceName = OTAUtils.getDeviceName(mContext!!)
        OTAUtils.logInfo("deviceName: " + deviceName)
        if (!deviceName.isEmpty()) {
            try {
                val otaUrl = OTAConfig.getInstance(mContext!!).otaUrl
                val ipstream = OTAUtils.downloadURL(otaUrl)
                    val releaseType = OTAConfig.getInstance(mContext!!).releaseType
                    device = OTAParser.instance.parse(ipstream, deviceName, releaseType)
                    ipstream.close()
            } catch (e: IOException) {
                OTAUtils.logError(e)
            } catch (e: XmlPullParserException) {
                OTAUtils.logError(e)
            }

        }

        return device
    }

    override fun onPostExecute(device: OTADevice?) {
        super.onPostExecute(device)

        if (device == null) {
            showToast(R.string.check_update_failed)
        } else {
            val latestVersion = device.latestVersion
            val updateAvailable = OTAVersion.checkServerVersion(latestVersion!!, this.mContext!!)
            if (updateAvailable) {
                showNotification(mContext)
            } else {
                showToast(R.string.no_update_available)
            }
            AppConfig.persistLatestVersion(latestVersion, mContext!!)
            LinkConfig.persistLinks(device.links, mContext!!)
        }

        AppConfig.persistLastCheck(this.mContext!!)

        hideWaitDialog()

        mInstance = null
    }

    override fun onCancelled() {
        super.onCancelled()
        mInstance = null
    }

    private fun showWaitDialog() {
        if (!mIsBackgroundThread) {
            val msg = mHandler.obtainMessage(WaitDialogHandler.MSG_SHOW_DIALOG)
            msg.obj = mContext
            mHandler.sendMessage(msg)
        }
    }

    private fun hideWaitDialog() {
        if (!mIsBackgroundThread) {
            val msg = mHandler.obtainMessage(WaitDialogHandler.MSG_CLOSE_DIALOG)
            mHandler.sendMessage(msg)
        }
    }

    private fun showToast(messageId: Int) {
        if (!mIsBackgroundThread) {
            OTAUtils.toast(messageId, mContext)
        }
    }

    private fun showNotification(context: Context?) {
        val builder = Notification.Builder(context)
        builder.setContentTitle(context!!.getString(R.string.notification_title))
        builder.setContentText(context.getString(R.string.notification_message))
        builder.setSmallIcon(R.drawable.infiniteos)
        builder.setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.infiniteos))

        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_ONE_SHOT)
        builder.setContentIntent(pendingIntent)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = builder.build()
        notification.flags = notification.flags or Notification.FLAG_AUTO_CANCEL
        notificationManager.notify(1000001, notification)
    }

    companion object {

        @SuppressLint("StaticFieldLeak")
        private var mInstance: CheckUpdateTask? = null

        fun getInstance(isBackgroundThread: Boolean): CheckUpdateTask {
            if (mInstance == null) {
                mInstance = CheckUpdateTask(isBackgroundThread)
            }
            return mInstance as CheckUpdateTask
        }

        private fun isConnectivityAvailable(context: Context?): Boolean {
            val connMgr = context!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val netInfo = connMgr.activeNetworkInfo
            return netInfo != null && netInfo.isConnected
        }
    }
}
