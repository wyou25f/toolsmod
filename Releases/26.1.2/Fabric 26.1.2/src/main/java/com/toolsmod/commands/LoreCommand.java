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
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LoreCommand {

    private static final Map<String, ChatFormatting> COLORS_RU = new LinkedHashMap<>();
    private static final Map<String, ChatFormatting> COLORS_EN = new LinkedHashMap<>();

    static {
        COLORS_RU.put("чёрный",            ChatFormatting.BLACK);
        COLORS_RU.put("тёмно_синий",       ChatFormatting.DARK_BLUE);
        COLORS_RU.put("тёмно_зелёный",     ChatFormatting.DARK_GREEN);
        COLORS_RU.put("бирюзовый",         ChatFormatting.DARK_AQUA);
        COLORS_RU.put("тёмно_красный",     ChatFormatting.DARK_RED);
        COLORS_RU.put("тёмно_фиолетовый",  ChatFormatting.DARK_PURPLE);
        COLORS_RU.put("золотой",           ChatFormatting.GOLD);
        COLORS_RU.put("серый",             ChatFormatting.GRAY);
        COLORS_RU.put("тёмно_серый",       ChatFormatting.DARK_GRAY);
        COLORS_RU.put("синий",             ChatFormatting.BLUE);
        COLORS_RU.put("зелёный",           ChatFormatting.GREEN);
        COLORS_RU.put("голубой",           ChatFormatting.AQUA);
        COLORS_RU.put("красный",           ChatFormatting.RED);
        COLORS_RU.put("розовый",           ChatFormatting.LIGHT_PURPLE);
        COLORS_RU.put("жёлтый",           ChatFormatting.YELLOW);
        COLORS_RU.put("белый",             ChatFormatting.WHITE);

        COLORS_EN.put("black",        ChatFormatting.BLACK);
        COLORS_EN.put("dark_blue",    ChatFormatting.DARK_BLUE);
        COLORS_EN.put("dark_green",   ChatFormatting.DARK_GREEN);
        COLORS_EN.put("dark_aqua",    ChatFormatting.DARK_AQUA);
        COLORS_EN.put("dark_red",     ChatFormatting.DARK_RED);
        COLORS_EN.put("dark_purple",  ChatFormatting.DARK_PURPLE);
        COLORS_EN.put("gold",         ChatFormatting.GOLD);
        COLORS_EN.put("gray",         ChatFormatting.GRAY);
        COLORS_EN.put("dark_gray",    ChatFormatting.DARK_GRAY);
        COLORS_EN.put("blue",         ChatFormatting.BLUE);
        COLORS_EN.put("green",        ChatFormatting.GREEN);
        COLORS_EN.put("aqua",         ChatFormatting.AQUA);
        COLORS_EN.put("red",          ChatFormatting.RED);
        COLORS_EN.put("light_purple", ChatFormatting.LIGHT_PURPLE);
        COLORS_EN.put("yellow",       ChatFormatting.YELLOW);
        COLORS_EN.put("white",        ChatFormatting.WHITE);
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("tools")
                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_MODERATOR))
                .then(
                    Commands.literal("lore")
                        .then(
                            Commands.literal("clear")
                                .executes(LoreCommand::executeClear)
                        )
                        .then(
                            Commands.argument("text", StringArgumentType.string())
                                .then(
                                    Commands.argument("color", StringArgumentType.string())
                                        .suggests((ctx, builder) -> {
                                            String lang = PlayerLang.RU;
                                            try {
                                                ServerPlayer p = ctx.getSource().getPlayerOrException();
                                                lang = PlayerLang.get(p.getUUID());
                                            } catch (CommandSyntaxException ignored) {}

                                            Map<String, ChatFormatting> colors = lang.equals(PlayerLang.RU)
                                                ? COLORS_RU : COLORS_EN;

                                            String input = builder.getRemaining()
                                                .replace("\"", "").toLowerCase();

                                            colors.keySet().stream()
                                                .filter(k -> k.startsWith(input))
                                                .forEach(k -> {
                                                    boolean needsQuotes = !k.chars()
                                                        .allMatch(c -> (c >= 'a' && c <= 'z')
                                                            || (c >= 'A' && c <= 'Z')
                                                            || (c >= '0' && c <= '9')
                                                            || c == '_' || c == '-' || c == '.');
                                                    builder.suggest(needsQuotes ? "\"" + k + "\"" : k);
                                                });

                                            return builder.buildFuture();
                                        })
                                        .executes(LoreCommand::execute)
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

        ChatFormatting color = resolveColor(colorInput.toLowerCase());

        Component loreLine = Component.literal(text)
            .withStyle(style -> style.withColor(color).withItalic(false));

        ItemLore existing = stack.getOrDefault(DataComponents.LORE, ItemLore.EMPTY);
        List<Component> lines = new ArrayList<>(existing.lines());
        lines.add(loreLine);
        stack.set(DataComponents.LORE, new ItemLore(List.copyOf(lines)));

        String itemName = stack.getHoverName().getString();
        int lineCount = lines.size();

        ctx.getSource().sendSuccess(() -> Component.literal(
            lang.equals(PlayerLang.RU)
                ? "§aЛор добавлен к §e" + itemName + " §7(строк: §e" + lineCount + "§7)"
                : "§aLore added to §e" + itemName + " §7(lines: §e" + lineCount + "§7)"
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

        stack.set(DataComponents.LORE, ItemLore.EMPTY);
        String itemName = stack.getHoverName().getString();

        ctx.getSource().sendSuccess(() -> Component.literal(
            lang.equals(PlayerLang.RU)
                ? "§aЛор очищен у §e" + itemName
                : "§aLore cleared from §e" + itemName
        ), false);
        return 1;
    }

    private static ChatFormatting resolveColor(String input) {
        if (COLORS_RU.containsKey(input)) return COLORS_RU.get(input);
        if (COLORS_EN.containsKey(input)) return COLORS_EN.get(input);
        return ChatFormatting.WHITE;
    }
}
