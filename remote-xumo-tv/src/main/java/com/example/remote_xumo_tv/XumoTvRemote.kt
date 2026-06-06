package com.example.remote_xumo_tv

import android.content.Context
import android.os.Build
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

class XumoTvRemote private constructor(
    private val store: XumoTokenStore,
    private val service: XumoMobilewareService,
    private val deviceName: String,
) {
    private val tokenMutex = Mutex()

    @Volatile
    private var cachedToken: XumoAccessToken? = null

    val isPaired: Boolean
        get() = !store.refreshToken.isNullOrEmpty()

    suspend fun pair(code: String): Result<Unit> = runCatching {
        val refreshToken = service.submitPairing(
            code = code.trim().uppercase(),
            pairingName = deviceName,
            installationId = UUID.randomUUID().toString(),
        )
        store.refreshToken = refreshToken
        cachedToken = null
    }

    suspend fun unpair(): Result<Unit> = runCatching {
        store.refreshToken?.let { rt -> runCatching { service.revokePairing(rt) } }
        store.clear()
        cachedToken = null
    }

    suspend fun press(key: XumoKey): Result<Unit> = runCatching {
        service.pressKey(accessToken(), key.code)
    }

    suspend fun search(phrase: String): Result<Unit> = runCatching {
        service.sendVoiceText(accessToken(), phrase)
    }

    suspend fun checkAlive(): Result<XumoDeviceStatus> = runCatching {
        service.checkAlive(accessToken())
    }

    private suspend fun accessToken(): String = tokenMutex.withLock {
        val refreshToken = store.refreshToken ?: throw XumoNotPairedException()
        val now = System.currentTimeMillis() / 1000
        cachedToken?.let { if (it.isValid(now)) return it.value }
        try {
            service.refreshAccessToken(refreshToken).also { cachedToken = it }.value
        } catch (e: XumoGrpcException) {
            store.clear()
            cachedToken = null
            throw e
        }
    }

    companion object {
        fun create(
            context: Context,
            deviceName: String = Build.MODEL ?: "Android Remote",
        ): XumoTvRemote = XumoTvRemote(
            store = XumoTokenStore(context),
            service = XumoMobilewareService.getInstance(),
            deviceName = deviceName,
        )
    }
}
