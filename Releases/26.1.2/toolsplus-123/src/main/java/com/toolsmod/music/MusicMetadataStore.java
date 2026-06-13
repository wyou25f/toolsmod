package com.toolsmod.music;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public final class MusicMetadataStore {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private MusicMetadataStore() {}

    public static Path metaFile(Path serverRoot, String id) {
        return serverRoot.resolve("toolsplus").resolve("music_input").resolve(id + ".meta.json");
    }

    public static void write(Path serverRoot, MusicMetadata meta) throws IOException {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", meta.id());
        obj.addProperty("title", meta.title());
        obj.addProperty("artist", meta.artist());
        obj.addProperty("album", meta.album());
        obj.addProperty("durationSeconds", meta.durationSeconds());

        Path file = metaFile(serverRoot, meta.id());
        Files.createDirectories(file.getParent());
        Files.writeString(file, GSON.toJson(obj), StandardCharsets.UTF_8);
    }

    public static Optional<MusicMetadata> read(Path serverRoot, String id) {
        Path file = metaFile(serverRoot, id);
        if (!Files.exists(file)) return Optional.empty();

        try {
            String json = Files.readString(file, StandardCharsets.UTF_8);
            JsonObject obj = GSON.fromJson(json, JsonObject.class);
            return Optional.of(new MusicMetadata(
                obj.get("id").getAsString(),
                obj.get("title").getAsString(),
                obj.get("artist").getAsString(),
                obj.get("album").getAsString(),
                obj.get("durationSeconds").getAsDouble()
            ));
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}
