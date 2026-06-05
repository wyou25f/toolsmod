package com.toolsmod.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.toolsmod.EnchantNames;
import com.toolsmod.PlayerLang;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.Map;
import java.util.Optional;

public class EnchantCommand {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            LangCommand.register(dispatcher);
            dispatcher.register(
                CommandManager.literal("tools")
                    .requires(source -> source.hasPermissionLevel(2))
                    .then(
                        CommandManager.literal("enchant")
                            .then(
                                CommandManager.argument("enchantment", StringArgumentType.string())
                                    .suggests((ctx, builder) -> {
                                        String lang = PlayerLang.RU;
                                        try {
                                            ServerPlayerEntity p = ctx.getSource().getPlayer();
                                            lang = PlayerLang.get(p.getUuid());
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
                                        CommandManager.argument("level", IntegerArgumentType.integer(1, 255))
                                            .executes(EnchantCommand::execute)
                                    )
                            )
                    )
            );
        });
    }

    private static int execute(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        String input = StringArgumentType.getString(ctx, "enchantment");
        int level = IntegerArgumentType.getInteger(ctx, "level");
        String lang = PlayerLang.get(player.getUuid());

        String mcId = EnchantNames.resolve(input);
        String[] parts = mcId.split(":", 2);
        Identifier id = new Identifier(parts[0], parts[1]);

        Optional<Enchantment> enchOpt = Registry.ENCHANTMENT.getOrEmpty(id);

        if (!enchOpt.isPresent()) {
            String hint = lang.equals(PlayerLang.RU)
                ? "острота, защита, нерушимость..."
                : "sharpness, protection, unbreaking...";
            ctx.getSource().sendError(new LiteralText(
                "§cНе найдено: §e" + input + "\n§7Примеры: §e" + hint
            ));
            return 0;
        }

        ItemStack stack = player.getMainHandStack();
        if (stack.isEmpty()) {
            ctx.getSource().sendError(new LiteralText(
                lang.equals(PlayerLang.RU) ? "§cВозьми предмет в руку!" : "§cHold an item in your hand!"
            ));
            return 0;
        }

        Enchantment ench = enchOpt.get();
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.get(stack);
        enchantments.put(ench, level);
        EnchantmentHelper.set(enchantments, stack);

        String enchantName = ench.getName(level).getString();
        String itemName = stack.getName().getString();

        ctx.getSource().sendFeedback(new LiteralText(
            lang.equals(PlayerLang.RU)
                ? "§aПрименено §e" + enchantName + " §7(ур. §e" + level + "§7) §aна §e" + itemName
                : "§aApplied §e" + enchantName + " §7(lvl §e" + level + "§7) §ato §e" + itemName
        ), true);
        return 1;
    }
}
