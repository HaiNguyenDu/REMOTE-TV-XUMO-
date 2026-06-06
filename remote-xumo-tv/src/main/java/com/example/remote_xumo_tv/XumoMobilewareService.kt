package com.example.remote_xumo_tv

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

internal class XumoMobilewareService private constructor() {

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    suspend fun submitPairing(code: String, pairingName: String, installationId: String): String {

        val codeMsg = Proto.Writer().string(1, code).toByteArray()
        val body = Proto.Writer()
            .message(1, codeMsg)
            .string(8, pairingName)
            .string(9, installationId)
            .toByteArray()
        val resp = Grpc.unary(
            client, XumoConstants.BASE_URL, "SubmitPairing",
            XumoConstants.auth(XumoConstants.SCHEME_1, XumoConstants.APP_PAIRING_TOKEN),
            body
        )

        return Proto.firstString(resp, 1)
    }

    suspend fun refreshAccessToken(refreshToken: String): XumoAccessToken {
        val resp = Grpc.unary(
            client, XumoConstants.BASE_URL, "RefreshAccessToken",
            XumoConstants.auth(XumoConstants.SCHEME_2, refreshToken),
            ByteArray(0)
        )

        val token = Proto.firstString(resp, 1)
        val expiresMsg = Proto.firstMessage(resp, 2)
        val expires = if (expiresMsg.isNotEmpty()) Proto.firstVarint(expiresMsg, 1) else 0L
        return XumoAccessToken(token, expires)
    }

    suspend fun pressKey(accessToken: String, key: String) {
        val body = Proto.Writer().string(1, key).toByteArray()
        Grpc.unary(
            client, XumoConstants.BASE_URL, "PressKey",
            XumoConstants.auth(XumoConstants.SCHEME_3, accessToken),
            body
        )
    }

    suspend fun checkAlive(accessToken: String): XumoDeviceStatus {
        val resp = Grpc.unary(
            client, XumoConstants.BASE_URL, "CheckAlive",
            XumoConstants.auth(XumoConstants.SCHEME_3, accessToken),
            ByteArray(0)
        )

        return when (Proto.firstVarint(resp, 1).toInt()) {
            2 -> XumoDeviceStatus.ONLINE
            1 -> XumoDeviceStatus.OFFLINE
            else -> XumoDeviceStatus.UNKNOWN
        }
    }

    suspend fun sendVoiceText(accessToken: String, phrase: String) {
        val body = Proto.Writer().string(1, phrase).toByteArray()
        Grpc.unary(
            client, XumoConstants.BASE_URL, "SendVoiceText",
            XumoConstants.auth(XumoConstants.SCHEME_3, accessToken),
            body
        )
    }

    suspend fun revokePairing(refreshToken: String) {
        Grpc.unary(
            client, XumoConstants.BASE_URL, "RevokePairing",
            XumoConstants.auth(XumoConstants.SCHEME_2, refreshToken),
            ByteArray(0)
        )
    }

    companion object {
        @Volatile
        private var INSTANCE: XumoMobilewareService? = null

        fun getInstance(): XumoMobilewareService =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: XumoMobilewareService().also { INSTANCE = it }
            }
    }
}
