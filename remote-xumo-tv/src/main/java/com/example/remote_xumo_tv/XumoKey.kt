package com.example.remote_xumo_tv

enum class XumoKey(val code: String) {
    INPUT("Input"),
    POWER("Power"),
    VOLUME_UP("VolUp"),
    VOLUME_DOWN("VolDown"),
    MUTE("Mute"),
    GEAR("Gear"),
    HOME("Home"),
    ELLIPSES("Ellipses"),
    DPAD_LEFT("DpadLeft"),
    DPAD_UP("DpadUp"),
    DPAD_RIGHT("DpadRight"),
    DPAD_DOWN("DpadDown"),
    DPAD_CENTER("DpadCenter"),
    BACK("Back"),
    PLUS("Plus"),
    VOICE("Voice"),
    APP_KEY_1("Appkey1"),
    APP_KEY_2("Appkey2"),
    APP_KEY_3("Appkey3"),
    APP_KEY_4("Appkey4"),
    APP_KEY_5("Appkey5"),
    APP_KEY_6("Appkey6"),
    NUM_0("Num0"),
    NUM_1("Num1"),
    NUM_2("Num2"),
    NUM_3("Num3"),
    NUM_4("Num4"),
    NUM_5("Num5"),
    NUM_6("Num6"),
    NUM_7("Num7"),
    NUM_8("Num8"),
    NUM_9("Num9"),
    MIC("Mic Button");

    companion object {
        fun number(n: Int): XumoKey = when (n) {
            0 -> NUM_0; 1 -> NUM_1; 2 -> NUM_2; 3 -> NUM_3; 4 -> NUM_4
            5 -> NUM_5; 6 -> NUM_6; 7 -> NUM_7; 8 -> NUM_8; else -> NUM_9
        }

        fun getListApp(): List<XumoApp> {
            return listOf(
                XumoApp(APP_KEY_1,R.drawable.app_netflix),
                XumoApp(APP_KEY_2,R.drawable.app_peacock),
                XumoApp(APP_KEY_3,R.drawable.app_disney),
                XumoApp(APP_KEY_4,R.drawable.app_prime_video),
                XumoApp(APP_KEY_5,R.drawable.app_xumo_play),
                XumoApp(APP_KEY_6,R.drawable.app_youtube),
            )
        }
    }
}
