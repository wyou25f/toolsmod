package com.toolsmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.toolsmod.PlayerLang;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class NameCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("tools")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(
                    Commands.literal("name")
                        .then(Commands.literal("clear").executes(NameCommand::executeClear))
                        .then(
                            Commands.argument("text", StringArgumentType.string())
                                .suggests((ctx, builder) -> {
                                    if (builder.getRemaining().isEmpty()) {
                                        String lang = PlayerLang.RU;
                                        try {
                                            lang = PlayerLang.get(ctx.getSource().getPlayerOrException().getUUID());
                                        } catch (CommandSyntaxException ignored) {}
                                        builder.suggest(
                                            lang.equals(PlayerLang.RU) ? "\"Название предмета\"" : "\"Item name\"",
                                            Component.literal(lang.equals(PlayerLang.RU)
                                                ? "Кириллицу — в кавычках"
                                                : "Wrap in quotes")
                                        );
                                    }
                                    return builder.buildFuture();
                                })
                                .then(
                                    Commands.argument("color", StringArgumentType.string())
                                        .suggests(LoreCommand::suggestColors)
                                        .executes(NameCommand::execute)
                                )
                        )
                )
        );
    }

    private static int execute(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        String text = StringArgumentType.getString(ctx, "text");
        String colorInput = StringArgumentType.getString(ctx, "color");
        String lang = PlayerLang.get(player.getUUID());

        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal(
                lang.equals(PlayerLang.RU) ? "§cВозьми предмет в руку!" : "§cHold an item in your hand!"
            ));
            return 0;
        }

        ChatFormatting color = LoreCommand.resolveColor(colorInput.toLowerCase());
        Component name = Component.literal(text)
            .withStyle(style -> style.withColor(color).withItalic(false));

        stack.set(DataComponents.CUSTOM_NAME, name);

        ctx.getSource().sendSuccess(() -> Component.literal(
            lang.equals(PlayerLang.RU)
                ? "§aНазвание изменено на §e" + text
                : "§aItem renamed to §e" + text
        ), false);
        return 1;
    }

    private static int executeClear(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        String lang = PlayerLang.get(player.getUUID());

        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal(
                lang.equals(PlayerLang.RU) ? "§cВозьми предмет в руку!" : "§cHold an item in your hand!"
            ));
            return 0;
        }

        stack.remove(DataComponents.CUSTOM_NAME);

        ctx.getSource().sendSuccess(() -> Component.literal(
            lang.equals(PlayerLang.RU)
                ? "§aНазвание сброшено у §e" + stack.getHoverName().getString()
                : "§aName reset for §e" + stack.getHoverName().getString()
        ), false);
        return 1;
    }
}
