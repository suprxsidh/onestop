package com.suprxsidh.onestop.gestures.ui.tile

import com.suprxsidh.onestop.gestures.model.GestureSettings

object GesturesTileMapper {
    fun toGesturesTileState(settings: GestureSettings): GesturesTileState =
        GesturesTileState(
            enabled = settings.enabled,
            suppressedAppCount = settings.blockedPackages.size
        )
}
