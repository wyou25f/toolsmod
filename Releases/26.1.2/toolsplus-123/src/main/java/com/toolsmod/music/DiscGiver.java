package com.toolsmod.music;

public final class DiscGiver {

    private DiscGiver() {}

    public static String buildGiveCommand(String playerName, MusicMetadata meta) {
        return "give " + playerName
            + " minecraft:music_disc_13[jukebox_playable={song:\"toolsplus:" + meta.id() + "\"},"
            + "custom_name='\"" + escape(meta.title()) + "\"',"
            + "lore=['\"" + escape(meta.displayLore()) + "\"']] 1";
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
