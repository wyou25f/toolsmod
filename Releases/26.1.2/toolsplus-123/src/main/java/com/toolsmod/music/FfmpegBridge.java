package com.toolsmod.music;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FfmpegBridge {

    private FfmpegBridge() {}

    public static boolean isAvailable() {
        return run("ffmpeg", "-version") && run("ffprobe", "-version");
    }

    private static boolean run(String... cmd) {
        try {
            Process p = new ProcessBuilder(cmd).redirectErrorStream(true).start();
            p.getInputStream().readAllBytes();
            return p.waitFor() == 0;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    public static MusicMetadata probe(Path input, String fallbackId) throws IOException, InterruptedException {
        Process p = new ProcessBuilder(
            "ffprobe", "-v", "quiet", "-print_format", "json", "-show_format", input.toString()
        ).start();
        String json = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        p.waitFor();

        String title = fallbackId;
        String artist = "Tools+";
        String album = "";
        double duration = 30;

        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        JsonObject format = root.has("format") ? root.getAsJsonObject("format") : null;

        if (format != null) {
            if (format.has("duration")) {
                duration = format.get("duration").getAsDouble();
            }
            JsonObject tags = format.has("tags") ? format.getAsJsonObject("tags") : null;
            if (tags != null) {
                title = readTag(tags, "title", title);
                artist = readTag(tags, "artist", artist);
                album = readTag(tags, "album", album);
            }
        }

        return new MusicMetadata(fallbackId, title, artist, album, duration);
    }

    private static String readTag(JsonObject tags, String key, String fallback) {
        if (tags.has(key)) return tags.get(key).getAsString();
        String upper = key.substring(0, 1).toUpperCase() + key.substring(1);
        if (tags.has(upper)) return tags.get(upper).getAsString();
        return fallback;
    }

    public static void convertToOgg(Path input, Path output) throws IOException, InterruptedException {
        Files.createDirectories(output.getParent());
        Process p = new ProcessBuilder(
            "ffmpeg", "-y", "-i", input.toString(),
            "-vn", "-c:a", "libvorbis", "-q:a", "5", "-ar", "44100",
            output.toString()
        ).redirectErrorStream(true).start();
        p.getInputStream().readAllBytes();
        int code = p.waitFor();
        if (code != 0) {
            throw new IOException("ffmpeg exit code " + code);
        }
    }
}
