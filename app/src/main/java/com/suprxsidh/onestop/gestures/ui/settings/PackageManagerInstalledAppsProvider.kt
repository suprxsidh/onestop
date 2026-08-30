package com.suprxsidh.onestop.gestures.ui.settings

import android.content.Context
import android.content.pm.ApplicationInfo

class PackageManagerInstalledAppsProvider(private val context: Context) : InstalledAppsProvider {
    override fun listInstalledApps(): List<InstalledApp> {
        val pm = context.packageManager
        @Suppress("DEPRECATION")
        val apps: List<ApplicationInfo> = pm.getInstalledApplications(0)
        return apps
            .filter { pm.getLaunchIntentForPackage(it.packageName) != null }
            .map { InstalledApp(packageName = it.packageName, label = pm.getApplicationLabel(it).toString()) }
            .sortedBy { it.label.lowercase() }
    }
}
