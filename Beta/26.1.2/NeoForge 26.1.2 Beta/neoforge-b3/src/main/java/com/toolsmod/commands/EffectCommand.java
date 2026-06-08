package com.toolsmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.toolsmod.EffectNames;
import com.toolsmod.PlayerLang;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.Map;

public class EffectCommand {

    public static final String NBT_EFFECT = "tools_effect";
    public static final String NBT_LEVEL  = "tools_effect_level";

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("tools")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(
                    Commands.literal("effect")
                        .then(Commands.literal("clear").executes(EffectCommand::executeClear))
                        .then(
                            Commands.argument("effect", StringArgumentType.string())
                                .suggests((ctx, builder) -> {
                                    String lang = LoreCommand.getLang(ctx);
                                    Map<String, String> names = lang.equals(PlayerLang.RU)
                                        ? EffectNames.RU : EffectNames.EN;
                                    String input = builder.getRemaining()
                                        .replace("\"", "").toLowerCase();
                                    names.keySet().stream()
                                        .filter(k -> k.startsWith(input))
                                        .forEach(k -> {
                                            boolean needsQuotes = !k.chars().allMatch(c ->
                                                (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') ||
                                                (c >= '0' && c <= '9') || c == '_' || c == '-' || c == '.');
                                            builder.suggest(needsQuotes ? "\"" + k + "\"" : k);
                                        });
                                    return builder.buildFuture();
                                })
                                .then(
                                    Commands.argument("level", IntegerArgumentType.integer(1, 255))
                                        .executes(EffectCommand::execute)
                                )
                        )
                )
        );
    }

    private static int execute(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        String input = StringArgumentType.getString(ctx, "effect");
        int level    = IntegerArgumentType.getInteger(ctx, "level");
        String lang  = PlayerLang.get(player.getUUID());

        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal(
                lang.equals(PlayerLang.RU) ? "§cВозьми предмет в руку!" : "§cHold an item in your hand!"
            ));
            return 0;
        }

        String mcId = EffectNames.resolve(input);
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putString(NBT_EFFECT, mcId);
        tag.putInt(NBT_LEVEL, level);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        ctx.getSource().sendSuccess(() -> Component.literal(
            lang.equals(PlayerLang.RU)
                ? "§aЭффект §e" + mcId + " §7(ур. §e" + level + "§7) §aзаписан на §e" + stack.getHoverName().getString()
                : "§aEffect §e" + mcId + " §7(lvl §e" + level + "§7) §abound to §e" + stack.getHoverName().getString()
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

        CustomData existing = stack.get(DataComponents.CUSTOM_DATA);
        if (existing != null) {
            CompoundTag tag = existing.copyTag();
            tag.remove(NBT_EFFECT);
            tag.remove(NBT_LEVEL);
            if (tag.isEmpty()) stack.remove(DataComponents.CUSTOM_DATA);
            else stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }

        ctx.getSource().sendSuccess(() -> Component.literal(
            lang.equals(PlayerLang.RU) ? "§aЭффект снят с предмета" : "§aEffect removed from item"
        ), false);
        return 1;
    }
}
