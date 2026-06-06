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
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.Map;
import java.util.Optional;

public class EffectCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("tools")
                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_MODERATOR))
                .then(
                    Commands.literal("effect")
                        .then(
                            Commands.literal("clear")
                                .executes(EffectCommand::executeClear)
                        )
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
                                    Commands.argument("duration", IntegerArgumentType.integer(1, 99999))
                                        .then(
                                            Commands.argument("level", IntegerArgumentType.integer(1, 255))
                                                .executes(EffectCommand::execute)
                                        )
                                )
                        )
                )
        );
    }

    private static int execute(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        String input = StringArgumentType.getString(ctx, "effect");
        int durationSeconds = IntegerArgumentType.getInteger(ctx, "duration");
        int level = IntegerArgumentType.getInteger(ctx, "level");
        String lang = PlayerLang.get(player.getUUID());

        String mcId = EffectNames.resolve(input);
        if (!mcId.contains(":")) {
            ctx.getSource().sendFailure(Component.literal(
                lang.equals(PlayerLang.RU) ? "§cНеверный формат: §e" + input : "§cInvalid format: §e" + input
            ));
            return 0;
        }

        String[] parts = mcId.split(":", 2);
        Identifier id = Identifier.fromNamespaceAndPath(parts[0], parts[1]);

        HolderLookup.RegistryLookup<MobEffect> registry = ctx.getSource().getServer()
            .registryAccess().lookupOrThrow(Registries.MOB_EFFECT);

        Optional<Holder.Reference<MobEffect>> effectOpt = registry.get(
            ResourceKey.create(Registries.MOB_EFFECT, id)
        );

        if (effectOpt.isEmpty()) {
            String hint = lang.equals(PlayerLang.RU)
                ? "скорость, сила, регенерация..."
                : "speed, strength, regeneration...";
            ctx.getSource().sendFailure(Component.literal(
                "§cНе найдено: §e" + input + "\n§7Примеры: §e" + hint
            ));
            return 0;
        }

        Holder.Reference<MobEffect> effect = effectOpt.get();
        int durationTicks = durationSeconds * 20;
        int amplifier = level - 1;

        player.addEffect(new MobEffectInstance(effect, durationTicks, amplifier));

        String effectName = effect.value().getDisplayName().getString();

        ctx.getSource().sendSuccess(() -> Component.literal(
            lang.equals(PlayerLang.RU)
                ? "§aПрименено §e" + effectName + " §7(ур. §e" + level + "§7, §e" + durationSeconds + "с§7)"
                : "§aApplied §e" + effectName + " §7(lvl §e" + level + "§7, §e" + durationSeconds + "s§7)"
        ), false);
        return 1;
    }

    private static int executeClear(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        String lang = PlayerLang.get(player.getUUID());

        player.removeAllEffects();

        ctx.getSource().sendSuccess(() -> Component.literal(
            lang.equals(PlayerLang.RU) ? "§aВсе эффекты сняты" : "§aAll effects cleared"
        ), false);
        return 1;
    }
}
