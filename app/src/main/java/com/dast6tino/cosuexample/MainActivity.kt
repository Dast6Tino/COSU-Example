package com.dast6tino.cosuexample

import android.app.Activity
import android.os.Bundle
import android.os.BatteryManager
import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import android.app.admin.SystemUpdatePolicy
import android.os.UserManager
import android.app.ActivityManager
import android.content.pm.PackageManager
import android.widget.Toast
import android.provider.Settings
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : Activity() {
    private var mDevicePolicyManager: DevicePolicyManager? = null
    private var mPackageManager: PackageManager? = null
    val LOCK_ACTIVITY_KEY = "lock_activity"
    val FROM_LOCK_ACTIVITY = 1
    var stateLock = false

    // add a variable to keep the component name in the application.
    private var mAdminComponentName: ComponentName? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setActionBar(toolbar)

        mDevicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

        mPackageManager = this.packageManager

        mAdminComponentName = DeviceAdminReceiver.getComponentName(this)
        mDevicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        if (mDevicePolicyManager!!.isDeviceOwnerApp(packageName)) {
            setDefaultCosuPolicies(true)
        } else {
            Toast.makeText(applicationContext, "Application not set to device owner", Toast.LENGTH_SHORT).show()
        }

        val intent = intent

        if (intent.getIntExtra(LOCK_ACTIVITY_KEY, 0) == FROM_LOCK_ACTIVITY) {
            mDevicePolicyManager!!.clearPackagePersistentPreferredActivities(
                    mAdminComponentName!!, packageName)
            mPackageManager!!.setComponentEnabledSetting(
                    ComponentName(applicationContext, MainActivity::class.java),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.lock_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
            if (item.itemId == R.id.changeLockState) {
                onLockClick()
                true
            } else {
                super.onOptionsItemSelected(item)
            }

    fun onLockClick() {
        if (stateLock) {
            if (mDevicePolicyManager!!.isDeviceOwnerApp(applicationContext.packageName)) {
                val intentLock = Intent(applicationContext, MainActivity::class.java)
                mPackageManager!!.setComponentEnabledSetting(
                        ComponentName(applicationContext,
                                MainActivity::class.java),
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        PackageManager.DONT_KILL_APP)
                startActivity(intentLock)
            }

            if (mDevicePolicyManager!!.isLockTaskPermitted(applicationContext.packageName)) {
                val intentLock = Intent(applicationContext, MainActivity::class.java)
                startActivity(intentLock)
                finish()
            }
            stateLock = true
        } else {
            val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            if (am.lockTaskModeState == ActivityManager.LOCK_TASK_MODE_LOCKED) {
                stopLockTask()
            }
            setDefaultCosuPolicies(false)
            stateLock = false
        }
    }

    override fun onStart() {
        super.onStart()

        if (mDevicePolicyManager!!.isLockTaskPermitted(this.packageName)) {
            val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            if (am.lockTaskModeState == ActivityManager.LOCK_TASK_MODE_NONE) {
                startLockTask()
            }
        }
    }

    private fun setDefaultCosuPolicies(active: Boolean) {
        setUserRestriction(UserManager.DISALLOW_SAFE_BOOT, active)
        setUserRestriction(UserManager.DISALLOW_FACTORY_RESET, active)
        setUserRestriction(UserManager.DISALLOW_ADD_USER, active)
        setUserRestriction(UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA, active)
        setUserRestriction(UserManager.DISALLOW_ADJUST_VOLUME, active)

        mDevicePolicyManager!!.setKeyguardDisabled(mAdminComponentName!!, active)
        mDevicePolicyManager!!.setKeyguardDisabled(mAdminComponentName!!, active)

        enableStayOnWhilePluggedIn(active)

        if (active) {
            mDevicePolicyManager!!.setSystemUpdatePolicy(mAdminComponentName!!, SystemUpdatePolicy.createWindowedInstallPolicy(60, 120))
        } else {
            mDevicePolicyManager!!.setSystemUpdatePolicy(mAdminComponentName!!, null)
        }

        mDevicePolicyManager!!.setLockTaskPackages(mAdminComponentName!!, if (active) arrayOf(packageName) else arrayOf())

        val intentFilter = IntentFilter(Intent.ACTION_MAIN)
        intentFilter.addCategory(Intent.CATEGORY_HOME)
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT)

        if (active) {
            mDevicePolicyManager!!.addPersistentPreferredActivity(mAdminComponentName!!, intentFilter, ComponentName(packageName, MainActivity::class.java.name))
        } else {
            mDevicePolicyManager!!.clearPackagePersistentPreferredActivities(mAdminComponentName!!, packageName)
        }

    }

    private fun setUserRestriction(restriction: String, disallow: Boolean) {
        if (disallow) {
            mDevicePolicyManager!!.addUserRestriction(mAdminComponentName!!, restriction)
        } else {
            mDevicePolicyManager!!.clearUserRestriction(mAdminComponentName!!, restriction)
        }
    }

    private fun enableStayOnWhilePluggedIn(enabled: Boolean) {
        if (enabled) {
            mDevicePolicyManager!!.setGlobalSetting(
                    mAdminComponentName!!,
                    Settings.Global.STAY_ON_WHILE_PLUGGED_IN,
                    Integer.toString(BatteryManager.BATTERY_PLUGGED_AC
                            or BatteryManager.BATTERY_PLUGGED_USB
                            or BatteryManager.BATTERY_PLUGGED_WIRELESS))
        } else {
            mDevicePolicyManager!!.setGlobalSetting(
                    mAdminComponentName!!,
                    Settings.Global.STAY_ON_WHILE_PLUGGED_IN,
                    "0"
            )
        }
    }

    override fun onDestroy() {
        if (stateLock) onLockClick()
        super.onDestroy()
    }
}
