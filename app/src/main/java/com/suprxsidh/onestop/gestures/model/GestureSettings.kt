package com.suprxsidh.onestop.gestures.model

data class GestureSettings(
    val enabled: Boolean = true,
    val mappings: Map<GestureType, GlobalActionType> = emptyMap(),
    val blockedPackages: Set<String> = emptySet()
) {
    fun actionFor(gestureType: GestureType): GlobalActionType =
        mappings[gestureType] ?: GlobalActionType.NONE
}
