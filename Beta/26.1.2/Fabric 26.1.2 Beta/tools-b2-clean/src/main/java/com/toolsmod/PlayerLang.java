package com.toolsmod;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerLang {

    public static final String RU = "ru";
    public static final String EN = "en";

    private static final Map<UUID, String> LANGS = new ConcurrentHashMap<>();

    public static String get(UUID uuid) {
        return LANGS.getOrDefault(uuid, RU);
    }

    public static void set(UUID uuid, String lang) {
        LANGS.put(uuid, lang);
    }
}
