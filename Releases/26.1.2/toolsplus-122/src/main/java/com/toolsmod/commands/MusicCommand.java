package com.toolsmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.toolsmod.PlayerLang;
import com.toolsmod.music.FfmpegBridge;
import com.toolsmod.music.MusicMetadata;
import com.toolsmod.music.MusicPackManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class MusicCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("tools")
                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_MODERATOR))
                .then(
                    Commands.literal("music")
                        .then(
                            Commands.argument("id", StringArgumentType.word())
                                .executes(MusicCommand::execute)
                        )
                )
        );
    }

    private static int execute(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        String id = StringArgumentType.getString(ctx, "id").toLowerCase();
        String lang = PlayerLang.get(player.getUUID());

        if (!FfmpegBridge.isAvailable()) {
            ctx.getSource().sendFailure(Component.literal(
                lang.equals(PlayerLang.RU)
                    ? "§cffmpeg не найден! Установи: §esudo dnf install ffmpeg"
                    : "§cffmpeg not found! Install: §esudo dnf install ffmpeg"
            ));
            return 0;
        }

        Path serverRoot = ctx.getSource().getServer().getServerDirectory();
        Path input = serverRoot.resolve("toolsplus").resolve("music_input").resolve(id + ".mp4");

        if (!Files.exists(input)) {
            ctx.getSource().sendFailure(Component.literal(
                lang.equals(PlayerLang.RU)
                    ? "§cФайл не найден: §etoolsplus/music_input/" + id + ".mp4"
                    : "§cFile not found: §etoolsplus/music_input/" + id + ".mp4"
            ));
            return 0;
        }

        try {
            MusicMetadata meta = FfmpegBridge.probe(input, id);

            Path oggTemp = serverRoot.resolve("toolsplus").resolve("music_input").resolve(id + ".ogg");
            FfmpegBridge.convertToOgg(input, oggTemp);

            MusicPackManager.writeSound(serverRoot, meta, oggTemp);
            Files.deleteIfExists(oggTemp);

            Path worldDatapacks = ctx.getSource().getServer().getWorldPath(LevelResource.DATAPACK_DIR);
            MusicPackManager.writeJukeboxSong(worldDatapacks, meta);

            ctx.getSource().getServer().getCommands()
                .performPrefixedCommand(ctx.getSource(), "reload");

            String discCmd = "give " + player.getName().getString()
                + " minecraft:music_disc_13[jukebox_playable={song:\"" + "toolsplus:" + id + "\"},"
                + "custom_name='\"" + escape(meta.title()) + "\"',"
                + "lore=['\"" + escape(meta.displayLore()) + "\"']] 1";

            ctx.getSource().getServer().getCommands()
                .performPrefixedCommand(ctx.getSource(), discCmd);

            int durationSec = (int) meta.durationSeconds();

            ctx.getSource().sendSuccess(() -> Component.literal(
                lang.equals(PlayerLang.RU)
                    ? "§aПластинка §e" + meta.title() + " §7— §e" + meta.artist()
                        + " §7(" + durationSec + "с) §aготова!"
                    : "§aDisc §e" + meta.title() + " §7— §e" + meta.artist()
                        + " §7(" + durationSec + "s) §aready!"
            ), false);

            ctx.getSource().sendSuccess(() -> Component.literal(
                lang.equals(PlayerLang.RU)
                    ? "§7Раздай ресурспак: §etoolsplus/resourcepack/toolsplus_music.zip"
                    : "§7Distribute resourcepack: §etoolsplus/resourcepack/toolsplus_music.zip"
            ), false);

        } catch (IOException | InterruptedException e) {
            ctx.getSource().sendFailure(Component.literal("§cError: " + e.getMessage()));
            return 0;
        }

        return 1;
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
