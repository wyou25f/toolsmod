package com.toolsmod;

import com.toolsmod.commands.EnchantCommand;
import com.toolsmod.listener.HeldEffectListener;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(ToolsMod.MOD_ID)
public class ToolsMod {

    public static final String MOD_ID = "tools";

    public ToolsMod(IEventBus modEventBus) {
        NeoForge.EVENT_BUS.addListener(EnchantCommand::register);
        HeldEffectListener.register();
    }
}
