package com.toolsmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.toolsmod.PlayerLang;
import com.toolsmod.music.DiscGiver;
import com.toolsmod.music.MusicMetadata;
import com.toolsmod.music.MusicMetadataStore;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;

import java.nio.file.Path;
import java.util.Optional;

public class PlastinkaCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("tools")
                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_MODERATOR))
                .then(
                    Commands.literal("plastinka")
                        .then(
                            Commands.argument("id", StringArgumentType.word())
                                .executes(PlastinkaCommand::execute)
                        )
                )
        );
    }

    private static int execute(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        String id = StringArgumentType.getString(ctx, "id").toLowerCase();
        String lang = PlayerLang.get(player.getUUID());

        Path serverRoot = ctx.getSource().getServer().getServerDirectory();
        Optional<MusicMetadata> metaOpt = MusicMetadataStore.read(serverRoot, id);

        if (metaOpt.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal(
                lang.equals(PlayerLang.RU)
                    ? "§cНет данных для §e" + id + "§c. Сначала: §e/tools music " + id
                    : "§cNo data for §e" + id + "§c. First run: §e/tools music " + id
            ));
            return 0;
        }

        MusicMetadata meta = metaOpt.get();
        String discCmd = DiscGiver.buildGiveCommand(player.getName().getString(), meta);

        ctx.getSource().getServer().getCommands()
            .performPrefixedCommand(ctx.getSource(), discCmd);

        ctx.getSource().sendSuccess(() -> Component.literal(
            lang.equals(PlayerLang.RU)
                ? "§aПластинка §e" + meta.title() + " §7— §e" + meta.artist() + " §aвыдана!"
                : "§aDisc §e" + meta.title() + " §7— §e" + meta.artist() + " §agiven!"
        ), false);

        return 1;
    }
}
