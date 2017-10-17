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

package ota.os.infinite.infiniteota.dialogs

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Message

class WaitDialogHandler : Handler() {

    private var mContext: Context? = null

    private val otaDialogFragment: WaitDialogFragment?
        get() {
            if (mContext is Activity) {
                val activity = mContext as Activity?
                val fragment = activity!!.fragmentManager.findFragmentByTag(DIALOG_TAG)
                return fragment as WaitDialogFragment
            }
            return null
        }

    override fun handleMessage(msg: Message) {
        when (msg.what) {
            MSG_SHOW_DIALOG -> {
                mContext = msg.obj as Context
                if (mContext is Activity) {
                    val activity = mContext as Activity?

                    val ft = activity!!.fragmentManager.beginTransaction()
                    val prev = otaDialogFragment
                    if (prev != null) {
                        ft.remove(prev)
                    }
                    ft.addToBackStack(null)

                    val dialog = WaitDialogFragment.newInstance()
                    dialog.show(ft, DIALOG_TAG)
                }
            }
            MSG_CLOSE_DIALOG -> {
                val dialog = otaDialogFragment
                dialog?.dismissAllowingStateLoss()
            }
            else -> {
            }
        }
    }

    companion object {

        val MSG_SHOW_DIALOG = 0
        val MSG_CLOSE_DIALOG = 1

        private val DIALOG_TAG = WaitDialogFragment::class.java.name
    }
}
