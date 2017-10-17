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

package ota.os.infinite.infiniteota.scheduler

import android.content.Intent
import android.os.AsyncTask

import com.commonsware.cwac.wakeful.WakefulIntentService
import ota.os.infinite.infiniteota.tasks.CheckUpdateTask

class OTAService : WakefulIntentService("InfiniteOTA") {

    override fun doWakefulWork(intent: Intent) {
        val otaChecker = CheckUpdateTask.getInstance(true)
        if (otaChecker.status != AsyncTask.Status.RUNNING) {
            otaChecker.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, applicationContext)
        }
    }
}
