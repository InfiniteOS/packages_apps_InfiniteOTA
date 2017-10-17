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

@file:Suppress("DEPRECATION")

package ota.os.infinite.infiniteota.dialogs

import android.app.Dialog
import android.app.DialogFragment
import android.app.ProgressDialog
import android.content.DialogInterface
import android.os.Bundle
import ota.os.infinite.infiniteota.R

@Suppress("DEPRECATION")
class WaitDialogFragment : DialogFragment() {

    private val otaDialogListener: OTADialogListener?
        get() = if (activity is OTADialogListener) {
            activity as OTADialogListener
        } else null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateDialog(savedInstanceState: Bundle): Dialog {
        isCancelable = true

        val dialog = ProgressDialog(activity)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setMessage(activity.getString(R.string.dialog_message))
        return dialog
    }

    override fun onDestroyView() {
        // Work around bug: http://code.google.com/p/android/issues/detail?id=17423
        val dialog = dialog
        if (dialog != null && retainInstance) {
            dialog.setDismissMessage(null)
        }
        super.onDestroyView()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        if (otaDialogListener != null) {
            otaDialogListener!!.onProgressCancelled()
        }
    }

    interface OTADialogListener {
        fun onProgressCancelled()
    }

    companion object {

        fun newInstance(): WaitDialogFragment {
            return WaitDialogFragment()
        }
    }
}
