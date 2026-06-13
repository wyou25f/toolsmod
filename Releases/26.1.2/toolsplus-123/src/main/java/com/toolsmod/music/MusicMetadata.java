package com.toolsmod.music;

public record MusicMetadata(String id, String title, String artist, String album, double durationSeconds) {

    public String displayLore() {
        return album.isEmpty() ? artist : artist + " — " + album;
    }
}
