package com.toolsmod.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.toolsmod.EnchantNames;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Map;
import java.util.Optional;

public class EnchantCommand {

    public static void register(RegisterCommandsEvent event) {
        LangCommand.register(event.getDispatcher());
        event.getDispatcher().register(
            Commands.literal("tools")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(
                    Commands.literal("enchant")
                        .then(
                            Commands.argument("enchantment", StringArgumentType.string())
                                .suggests((ctx, builder) -> {
                                    String lang = PlayerLang.RU;
                                    try {
                                        ServerPlayer p = ctx.getSource().getPlayerOrException();
                                        lang = PlayerLang.get(p.getUUID());
                                    } catch (CommandSyntaxException ignored) {}

                                    Map<String, String> names = lang.equals(PlayerLang.RU)
                                        ? EnchantNames.RU : EnchantNames.EN;

                                    String input = builder.getRemaining()
                                        .replace("\"", "").toLowerCase();

                                    names.keySet().stream()
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
                                .then(
                                    Commands.argument("level", IntegerArgumentType.integer(1, 255))
                                        .executes(EnchantCommand::execute)
                                )
                        )
                )
        );
    }

    private static int execute(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        String input = StringArgumentType.getString(ctx, "enchantment");
        int level = IntegerArgumentType.getInteger(ctx, "level");
        String lang = PlayerLang.get(player.getUUID());

        String mcId = EnchantNames.resolve(input);
        String[] parts = mcId.split(":", 2);
        Identifier id = Identifier.fromNamespaceAndPath(parts[0], parts[1]);

        HolderLookup.RegistryLookup<Enchantment> registry = ctx.getSource().getServer()
            .registryAccess().lookupOrThrow(Registries.ENCHANTMENT);

        Optional<Holder.Reference<Enchantment>> enchOpt = registry.get(
            ResourceKey.create(Registries.ENCHANTMENT, id)
        );

        if (enchOpt.isEmpty()) {
            String hint = lang.equals(PlayerLang.RU)
                ? "острота, защита, нерушимость..."
                : "sharpness, protection, unbreaking...";
            ctx.getSource().sendFailure(Component.literal(
                "§cНе найдено: §e" + input + "\n§7Примеры: §e" + hint
            ));
            return 0;
        }

        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal(
                lang.equals(PlayerLang.RU) ? "§cВозьми предмет в руку!" : "§cHold an item in your hand!"
            ));
            return 0;
        }

        Holder.Reference<Enchantment> ench = enchOpt.get();
        EnchantmentHelper.updateEnchantments(stack, mutable -> mutable.set(ench, level));

        String enchantName = ench.value().description().getString();
        String itemName = stack.getHoverName().getString();

        ctx.getSource().sendSuccess(() -> Component.literal(
            lang.equals(PlayerLang.RU)
                ? "§aПрименено §e" + enchantName + " §7(ур. §e" + level + "§7) §aна §e" + itemName
                : "§aApplied §e" + enchantName + " §7(lvl §e" + level + "§7) §ato §e" + itemName
        ), true);
        return 1;
    }
}
