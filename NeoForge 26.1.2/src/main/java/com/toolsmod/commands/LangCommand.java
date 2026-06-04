package com.toolsmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.toolsmod.PlayerLang;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class LangCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("tools")
                .requires(source -> source.hasPermission(2))
                .then(
                    Commands.literal("lang")
                        .then(Commands.literal("ru").executes(ctx -> setLang(ctx, PlayerLang.RU)))
                        .then(Commands.literal("en").executes(ctx -> setLang(ctx, PlayerLang.EN)))
                )
        );
    }

    private static int setLang(CommandContext<CommandSourceStack> ctx, String lang) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        PlayerLang.set(player.getUUID(), lang);
        String msg = lang.equals(PlayerLang.RU)
            ? "§aЯзык: §eРусский §7— попробуй §e/tools enchant острота 10"
            : "§aLanguage: §eEnglish §7— try §e/tools enchant sharpness 10";
        ctx.getSource().sendSuccess(() -> Component.literal(msg), false);
        return 1;
    }
}