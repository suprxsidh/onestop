package com.suprxsidh.onestop.gestures.guard

import com.suprxsidh.onestop.gestures.model.GestureSettings
import com.suprxsidh.onestop.gestures.model.GestureType
import com.suprxsidh.onestop.gestures.model.GlobalActionType

object DispatchGuard {
    fun resolveAction(
        foregroundPackage: String?,
        settings: GestureSettings,
        gestureType: GestureType
    ): GlobalActionType? {
        if (!settings.enabled) return null
        if (foregroundPackage != null && foregroundPackage in settings.blockedPackages) return null
        val action = settings.actionFor(gestureType)
        return if (action == GlobalActionType.NONE) null else action
    }
}
