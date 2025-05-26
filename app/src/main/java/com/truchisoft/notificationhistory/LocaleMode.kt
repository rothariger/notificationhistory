package com.truchisoft.notificationhistory

import java.util.Locale

enum class LocaleMode(val locale: Locale) {
    SYSTEM(Locale.getDefault()),
    ENGLISH(Locale.ENGLISH),
    SPANISH(Locale("es")),
    GERMAN(Locale.GERMAN),
    FRENCH(Locale.FRENCH),
    ITALIAN(Locale.ITALIAN),
    PORTUGUESE(Locale("pt"));

    companion object {
        fun fromLocale(locale: Locale): LocaleMode {
            return when (locale.language) {
                "en" -> ENGLISH
                "es" -> SPANISH
                "de" -> GERMAN
                "fr" -> FRENCH
                "it" -> ITALIAN
                "pt" -> PORTUGUESE
                else -> SYSTEM
            }
        }
    }
}
