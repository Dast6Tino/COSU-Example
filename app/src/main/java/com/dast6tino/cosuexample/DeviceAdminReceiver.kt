package com.dast6tino.cosuexample

import android.content.ComponentName
import android.content.Context


/**
 * Created by oprv2 on 03.04.2018.
 */
class DeviceAdminReceiver : android.app.admin.DeviceAdminReceiver() {
    companion object {
        val TAG = "DeviceAdministrator"
        fun getComponentName(context: Context): ComponentName {
            return ComponentName(context.applicationContext, DeviceAdminReceiver::class.java)
        }
    }
}