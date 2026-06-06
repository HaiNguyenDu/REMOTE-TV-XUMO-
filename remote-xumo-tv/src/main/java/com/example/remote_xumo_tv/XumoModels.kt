package com.example.remote_xumo_tv

internal data class XumoAccessToken(
    val value: String,
    val expiresEpochSeconds: Long
) {
    fun isValid(nowEpochSeconds: Long, skewSeconds: Long = 30): Boolean =
        expiresEpochSeconds <= 0L || nowEpochSeconds < expiresEpochSeconds - skewSeconds
}

enum class XumoDeviceStatus { UNKNOWN, OFFLINE, ONLINE }

class XumoNotPairedException : IllegalStateException("Xumo tv not paired")
