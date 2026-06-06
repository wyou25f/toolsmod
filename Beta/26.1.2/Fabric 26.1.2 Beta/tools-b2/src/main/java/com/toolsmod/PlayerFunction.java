package com.toolsmod;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerFunction {

    private static final Map<UUID, Boolean> GUI_ENABLED = new ConcurrentHashMap<>();

    public static boolean isGUIEnabled(UUID uuid) {
        return GUI_ENABLED.getOrDefault(uuid, true);
    }

    public static void setGUIEnabled(UUID uuid, boolean enabled) {
        GUI_ENABLED.put(uuid, enabled);
    }
}
