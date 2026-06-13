package com.toolsmod.music;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class MusicPackManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String NAMESPACE = "toolsplus";

    private MusicPackManager() {}

    public static Path resourcePackRoot(Path serverRoot) {
        return serverRoot.resolve("toolsplus").resolve("resourcepack").resolve("toolsplus_music");
    }

    public static Path datapackRoot(Path worldDatapacksDir) {
        return worldDatapacksDir.resolve("toolsplus_music");
    }

    public static Path resourcePackZip(Path serverRoot) {
        return serverRoot.resolve("toolsplus").resolve("resourcepack").resolve("toolsplus_music.zip");
    }

    public static void writeSound(Path serverRoot, MusicMetadata meta, Path oggSource) throws IOException {
        Path rpRoot = resourcePackRoot(serverRoot);
        Path soundsDir = rpRoot.resolve("assets").resolve(NAMESPACE).resolve("sounds").resolve("music");
        Files.createDirectories(soundsDir);
        Files.copy(oggSource, soundsDir.resolve(meta.id() + ".ogg"), StandardCopyOption.REPLACE_EXISTING);

        writePackMcmeta(rpRoot, "Tools+ custom music", 88);
        updateSoundsJson(rpRoot, meta);
        zipResourcePack(serverRoot, rpRoot);
    }

    public static void writeJukeboxSong(Path worldDatapacksDir, MusicMetadata meta) throws IOException {
        Path dpRoot = datapackRoot(worldDatapacksDir);
        Path songDir = dpRoot.resolve("data").resolve(NAMESPACE).resolve("jukebox_song");
        Files.createDirectories(songDir);

        JsonObject song = new JsonObject();
        song.addProperty("sound_event", NAMESPACE + ":" + meta.id());
        JsonObject description = new JsonObject();
        description.addProperty("text", meta.title() + " — " + meta.artist());
        song.add("description", description);
        song.addProperty("length_in_seconds", Math.max(1.0, meta.durationSeconds()));
        song.addProperty("comparator_output_signal", 8);

        Files.writeString(songDir.resolve(meta.id() + ".json"), GSON.toJson(song), StandardCharsets.UTF_8);
        writePackMcmeta(dpRoot, "Tools+ custom jukebox songs", 107);
    }

    private static void updateSoundsJson(Path rpRoot, MusicMetadata meta) throws IOException {
        Path soundsJson = rpRoot.resolve("assets").resolve(NAMESPACE).resolve("sounds.json");

        JsonObject root = Files.exists(soundsJson)
            ? GSON.fromJson(Files.readString(soundsJson, StandardCharsets.UTF_8), JsonObject.class)
            : new JsonObject();

        JsonObject entry = new JsonObject();
        JsonArray sounds = new JsonArray();
        sounds.add(NAMESPACE + ":music/" + meta.id());
        entry.add("sounds", sounds);
        root.add(meta.id(), entry);

        Files.writeString(soundsJson, GSON.toJson(root), StandardCharsets.UTF_8);
    }

    private static void writePackMcmeta(Path root, String description, int format) throws IOException {
        Path mcmeta = root.resolve("pack.mcmeta");
        JsonObject pack = new JsonObject();
        JsonObject inner = new JsonObject();
        inner.addProperty("description", description);
        inner.addProperty("min_format", format);
        inner.addProperty("max_format", format);
        pack.add("pack", inner);
        Files.writeString(mcmeta, GSON.toJson(pack), StandardCharsets.UTF_8);
    }

    private static void zipResourcePack(Path serverRoot, Path rpRoot) throws IOException {
        Path zipPath = resourcePackZip(serverRoot);
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath))) {
            try (Stream<Path> walk = Files.walk(rpRoot)) {
                walk.filter(Files::isRegularFile).forEach(file -> {
                    String entryName = rpRoot.relativize(file).toString().replace('\\', '/');
                    try {
                        zos.putNextEntry(new ZipEntry(entryName));
                        Files.copy(file, zos);
                        zos.closeEntry();
                    } catch (IOException e) {
                        throw new java.io.UncheckedIOException(e);
                    }
                });
            }
        }
    }
}
