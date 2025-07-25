package com.github.djaquels.utils;

import com.github.djaquels.ui.Labels;

import java.util.HashMap;
import java.util.Locale;

public class LanguageUtils {

    private static final HashMap<String, String> languagesMap = new HashMap<>();
    static {
        languagesMap.put("en", "english");
        languagesMap.put("sv", "swedish");
        languagesMap.put("fr", "french");
        languagesMap.put("es", "spanish");
        languagesMap.put("de", "german");
    }

    // Returns the app language string, e.g., "english"
    public static String getLocalLanguage() {
        Locale currentLocale = Locale.getDefault();
        String language = currentLocale.getLanguage();
        return languagesMap.getOrDefault(language, "english");
    }

    // Returns the Labels instance for the current language
    public static Labels getWindowConfs() {
        String appLanguage = getLocalLanguage();
        return Labels.getInstance(appLanguage);
    }
}