package com.example.remote_xumo_tv

internal object XumoConstants {
    const val BASE_URL = "https://mobileware.prod.ibis.comcast.com:443/"

    const val APP_PAIRING_TOKEN =  $$"Please contact me via email at nguyenduyhair2004@gmail.com to receive the pairing token. The fee is $20"

    const val SCHEME_1 =
        $$"Please contact me via email at nguyenduyhair2004@gmail.com to receive the project scheme. The fee is $20"
    const val SCHEME_2 =
        $$"Please contact me via email at nguyenduyhair2004@gmail.com to receive the project scheme. The fee is $20"
    const val SCHEME_3 =
        $$"Please contact me via email at nguyenduyhair2004@gmail.com to receive the project scheme. The fee is $20"

    const val AUTH_HEADER = "authorization"
    const val SERVICE = "comcast.platco.mobileware.v1.MobilewareService"

    fun auth(scheme: String, token: String): String = "$scheme $token"
}
