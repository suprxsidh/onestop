package com.suprxsidh.onestop.gestures.ui.settings

data class InstalledApp(val packageName: String, val label: String)

fun interface InstalledAppsProvider {
    fun listInstalledApps(): List<InstalledApp>
}
