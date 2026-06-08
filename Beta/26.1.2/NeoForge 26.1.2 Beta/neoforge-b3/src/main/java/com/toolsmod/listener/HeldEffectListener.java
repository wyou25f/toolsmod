package com.toolsmod.listener;

import com.toolsmod.commands.EffectCommand;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class HeldEffectListener {

    private static final Map<UUID, Holder<MobEffect>> APPLIED = new ConcurrentHashMap<>();
    private static int tick = 0;

    public static void register() {
        NeoForge.EVENT_BUS.addListener(HeldEffectListener::onServerTick);
    }

    private static void onServerTick(ServerTickEvent.Post event) {
        if (++tick % 10 != 0) return;

        HolderLookup.RegistryLookup<MobEffect> lookup;
        try {
            lookup = event.getServer().registryAccess().lookupOrThrow(Registries.MOB_EFFECT);
        } catch (Exception e) {
            return;
        }

        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            ItemStack held = player.getMainHandItem();
            Holder<MobEffect> newEffect = null;
            int newLevel = 1;

            CustomData data = held.get(DataComponents.CUSTOM_DATA);
            if (data != null) {
                CompoundTag tag = data.copyTag();
                String effectId = tag.getString(EffectCommand.NBT_EFFECT).orElse("");
                if (!effectId.isEmpty() && effectId.contains(":")) {
                    newLevel = tag.getInt(EffectCommand.NBT_LEVEL).orElse(1);
                    String[] parts = effectId.split(":", 2);
                    Identifier id = Identifier.fromNamespaceAndPath(parts[0], parts[1]);
                    newEffect = lookup.get(ResourceKey.create(Registries.MOB_EFFECT, id)).orElse(null);
                }
            }

            Holder<MobEffect> previous = APPLIED.get(player.getUUID());

            if (newEffect != null) {
                if (previous != null && !previous.equals(newEffect)) player.removeEffect(previous);
                player.addEffect(new MobEffectInstance(newEffect, 60, newLevel - 1));
                APPLIED.put(player.getUUID(), newEffect);
            } else if (previous != null) {
                player.removeEffect(previous);
                APPLIED.remove(player.getUUID());
            }
        }
    }
}
