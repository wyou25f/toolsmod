package com.toolsmod.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.toolsmod.EnchantNames;
import com.toolsmod.PlayerLang;
import com.toolsmod.ToolsMod;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = ToolsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EnchantCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        LangCommand.register(event.getDispatcher());
        event.getDispatcher().register(
            Commands.literal("tools")
                .requires(source -> source.hasPermission(2))
                .then(
                    Commands.literal("enchant")
                        .then(
                            Commands.argument("enchantment", StringArgumentType.string())
                                .suggests((ctx, builder) -> {
                                    String lang = PlayerLang.RU;
                                    try {
                                        ServerPlayerEntity p = ctx.getSource().getPlayerOrException();
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

    private static int execute(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayerOrException();
        String input = StringArgumentType.getString(ctx, "enchantment");
        int level = IntegerArgumentType.getInteger(ctx, "level");
        String lang = PlayerLang.get(player.getUUID());

        String mcId = EnchantNames.resolve(input);
        String[] parts = mcId.split(":", 2);
        ResourceLocation id = new ResourceLocation(parts[0], parts[1]);

        Enchantment ench = ForgeRegistries.ENCHANTMENTS.getValue(id);

        if (ench == null) {
            String hint = lang.equals(PlayerLang.RU)
                ? "острота, защита, нерушимость..."
                : "sharpness, protection, unbreaking...";
            ctx.getSource().sendFailure(new StringTextComponent(
                "§cНе найдено: §e" + input + "\n§7Примеры: §e" + hint
            ));
            return 0;
        }

        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            ctx.getSource().sendFailure(new StringTextComponent(
                lang.equals(PlayerLang.RU) ? "§cВозьми предмет в руку!" : "§cHold an item in your hand!"
            ));
            return 0;
        }

        Map<Enchantment, Integer> enchants = new HashMap<>(EnchantmentHelper.getEnchantments(stack));
        enchants.put(ench, level);
        EnchantmentHelper.setEnchantments(enchants, stack);

        String enchantName = new TranslationTextComponent(ench.getDescriptionId()).getString();
        String itemName = stack.getDisplayName().getString();

        ctx.getSource().sendSuccess(new StringTextComponent(
            lang.equals(PlayerLang.RU)
                ? "§aПрименено §e" + enchantName + " §7(ур. §e" + level + "§7) §aна §e" + itemName
                : "§aApplied §e" + enchantName + " §7(lvl §e" + level + "§7) §ato §e" + itemName
        ), true);
        return 1;
    }
}
