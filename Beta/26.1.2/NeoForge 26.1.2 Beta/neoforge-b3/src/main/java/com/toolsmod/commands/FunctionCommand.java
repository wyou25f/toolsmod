package com.toolsmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.toolsmod.PlayerFunction;
import com.toolsmod.PlayerLang;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class FunctionCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("tools")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(
                    Commands.literal("function")
                        .then(Commands.literal("on") .executes(ctx -> setFunction(ctx, true)))
                        .then(Commands.literal("off").executes(ctx -> setFunction(ctx, false)))
                )
        );
    }

    private static int setFunction(CommandContext<CommandSourceStack> ctx, boolean enabled)
        throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        PlayerFunction.setGUIEnabled(player.getUUID(), enabled);
        String lang = PlayerLang.get(player.getUUID());

        ctx.getSource().sendSuccess(() -> Component.literal(enabled
            ? (lang.equals(PlayerLang.RU)
                ? "§aИнтерфейсы блоков: §eВКЛ §7— блоки открываются как обычно"
                : "§aBlock interfaces: §eON §7— blocks open normally")
            : (lang.equals(PlayerLang.RU)
                ? "§aИнтерфейсы блоков: §cВЫКЛ §7— режим строительства активен"
                : "§aBlock interfaces: §cOFF §7— building mode active")
        ), false);
        return 1;
    }
}
