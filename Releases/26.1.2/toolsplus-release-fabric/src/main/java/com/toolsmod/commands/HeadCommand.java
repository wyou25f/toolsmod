package com.toolsmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.toolsmod.PlayerLang;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;

public class HeadCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("tools")
                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_MODERATOR))
                .then(
                    Commands.literal("head")
                        .then(
                            Commands.argument("username", StringArgumentType.word())
                                .then(
                                    Commands.argument("amount", IntegerArgumentType.integer(1, 64))
                                        .executes(ctx -> execute(ctx,
                                            StringArgumentType.getString(ctx, "username"),
                                            IntegerArgumentType.getInteger(ctx, "amount")))
                                )
                                .executes(ctx -> execute(ctx,
                                    StringArgumentType.getString(ctx, "username"), 1))
                        )
                )
        );
    }

    private static int execute(CommandContext<CommandSourceStack> ctx, String username, int amount)
        throws CommandSyntaxException {

        ServerPlayer player = ctx.getSource().getPlayerOrException();
        String lang = PlayerLang.get(player.getUUID());

        String cmd = "give " + player.getName().getString()
            + " minecraft:player_head[profile={name:\"" + username + "\"}] " + amount;

        ctx.getSource().getServer().getCommands()
            .performPrefixedCommand(ctx.getSource(), cmd);

        ctx.getSource().sendSuccess(() -> Component.literal(
            lang.equals(PlayerLang.RU)
                ? "§aГолова §e" + username + " §7(x" + amount + "§7) §aдобавлена"
                : "§aHead of §e" + username + " §7(x" + amount + "§7) §agiven"
        ), false);
        return 1;
    }
}
