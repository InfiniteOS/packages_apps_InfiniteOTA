package ota.os.infinite.infiniteota

import android.annotation.SuppressLint
import android.os.Bundle
import android.preference.PreferenceActivity
import android.view.MenuItem
import ota.os.infinite.infiniteota.configs.LinkConfig
import ota.os.infinite.infiniteota.dialogs.WaitDialogFragment
import ota.os.infinite.infiniteota.fragments.InfiniteOTAFragment

@SuppressLint("ExportedPreferenceActivity")
class MainActivity : PreferenceActivity(), WaitDialogFragment.OTADialogListener, LinkConfig.LinkConfigListener {
    private var mFragment: InfiniteOTAFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mFragment = fragmentManager.findFragmentByTag(FRAGMENT_TAG) as InfiniteOTAFragment?
        if (mFragment == null) {
            fragmentManager.beginTransaction()
                    .replace(android.R.id.content, InfiniteOTAFragment(), FRAGMENT_TAG)
                    .commit()
        }
        //actionBar.show()
        //actionBar.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
        // Respond to the action bar's Up/Home button
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onProgressCancelled() {
        val fragment = fragmentManager.findFragmentByTag(FRAGMENT_TAG)
        if (fragment is WaitDialogFragment.OTADialogListener) {
            (fragment as WaitDialogFragment.OTADialogListener).onProgressCancelled()
        }
    }

    override fun onConfigChange() {
        val fragment = fragmentManager.findFragmentByTag(FRAGMENT_TAG)
        if (fragment is LinkConfig.LinkConfigListener) {
            (fragment as LinkConfig.LinkConfigListener).onConfigChange()
        }
    }

    companion object {

        private val FRAGMENT_TAG = InfiniteOTAFragment::class.java.name
    }
}
