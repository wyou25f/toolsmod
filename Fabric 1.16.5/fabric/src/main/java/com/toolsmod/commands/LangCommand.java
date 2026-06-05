package com.toolsmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.toolsmod.PlayerLang;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

public class LangCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("tools")
                .requires(source -> source.hasPermissionLevel(2))
                .then(
                    CommandManager.literal("lang")
                        .then(CommandManager.literal("ru").executes(ctx -> setLang(ctx, PlayerLang.RU)))
                        .then(CommandManager.literal("en").executes(ctx -> setLang(ctx, PlayerLang.EN)))
                )
        );
    }

    private static int setLang(CommandContext<ServerCommandSource> ctx, String lang) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        PlayerLang.set(player.getUuid(), lang);
        String msg = lang.equals(PlayerLang.RU)
            ? "§aЯзык: §eРусский §7— попробуй §e/tools enchant острота 10"
            : "§aLanguage: §eEnglish §7— try §e/tools enchant sharpness 10";
        ctx.getSource().sendFeedback(new LiteralText(msg), false);
        return 1;
    }
}
