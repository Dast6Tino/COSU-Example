package com.dast6tino.cosuexample

import android.app.Activity
import android.os.Bundle
import android.content.ComponentName
import android.widget.Toast
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import com.dast6tino.cosuexample.DeviceAdminReceiver.Companion.TAG
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Package.getPackage



class MainActivity : Activity() {
    var decorView: View? = null
    var mDpm: DevicePolicyManager? = null
    var isKioskEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d (TAG, "ComponentName =" + DeviceAdminReceiver.getComponentName (applicationContext))

        val deviceAdmin = ComponentName(this, DeviceAdminReceiver::class.java)
        mDpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        if (!mDpm!!.isAdminActive(deviceAdmin)) {
            Toast.makeText(this, "not_device_admin", Toast.LENGTH_SHORT).show()
        }

        if (mDpm!!.isDeviceOwnerApp(packageName)) {
            mDpm!!.setLockTaskPackages(deviceAdmin, arrayOf(packageName))
        } else {
            Toast.makeText(this, "not_device_owner", Toast.LENGTH_SHORT).show()
        }

        decorView = window.decorView

        webView.loadUrl("https://dast6tino.github.io/index")

        lockButton.setOnClickListener {
            enableKioskMode(isKioskEnabled)
        }
        nextActivityButton.setOnClickListener {
            startActivity(Intent(this, NextActivity::class.java))
            finish()
        }
        removeAdminReceiver.setOnClickListener {
            /*val dpm = this.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            dpm.clearDeviceOwnerApp(getPackage().toString())*/
            mDpm!!.clearDeviceOwnerApp(packageName)
        }
    }

    override fun onResume() {
        super.onResume()
        hideSystemUI()
    }

    private fun enableKioskMode(enabled: Boolean) {
        try {
            if (!enabled) {
                if (mDpm!!.isLockTaskPermitted(this.packageName)) {
                    startLockTask()
                    isKioskEnabled = true
                    lockButton.text = getString(R.string.exit_kiosk_mode)
                } else {
                    Toast.makeText(this, getString(R.string.kiosk_not_permitted), Toast.LENGTH_SHORT).show()
                }
            } else {
                stopLockTask()
                isKioskEnabled = false
                lockButton.text = getString(R.string.enter_kiosk_mode)
            }
        } catch (e: Exception) {
            // TODO: Log and handle appropriately
        }

    }

    // This snippet hides the system bars.
    private fun hideSystemUI() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        decorView?.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar

                or View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar

                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }
}
