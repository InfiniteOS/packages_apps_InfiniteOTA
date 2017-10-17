package ota.os.infinite.infiniteota.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.SystemClock

import com.commonsware.cwac.wakeful.WakefulIntentService
import ota.os.infinite.infiniteota.configs.AppConfig
import ota.os.infinite.infiniteota.utils.OTAUtils

class OTAListener : WakefulIntentService.AlarmListener {
    override fun getMaxAge(): Long {
        return  mIntervalValue*2
    }

    private var mIntervalValue = DEFAULT_INTERVAL_VALUE

    override fun scheduleAlarms(alarmManager: AlarmManager, pendingIntent: PendingIntent, context: Context) {
        mIntervalValue = AppConfig.getUpdateIntervalTime(context)
        if (mIntervalValue > 0) {
            OTAUtils.logInfo("InfiniteOTA is scheduled for every: $mIntervalValue ms")
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + 60000, mIntervalValue, pendingIntent)
        } else {
            OTAUtils.logInfo("InfiniteOTA is disabled")
        }
    }

    override fun sendWakefulWork(context: Context) {
        val connMan = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val netInfo = connMan.activeNetworkInfo
            if (netInfo != null && netInfo.isConnected) {
                val backgroundIntent = Intent(context, OTAService::class.java)
                WakefulIntentService.sendWakefulWork(context, backgroundIntent)
            }
        }

    companion object {

        val DEFAULT_INTERVAL_VALUE = AlarmManager.INTERVAL_HALF_DAY
    }
}
