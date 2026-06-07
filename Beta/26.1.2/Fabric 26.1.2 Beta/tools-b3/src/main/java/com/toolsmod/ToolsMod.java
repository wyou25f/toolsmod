package com.toolsmod;

import com.toolsmod.commands.EnchantCommand;
import com.toolsmod.listener.HeldEffectListener;
import net.fabricmc.api.ModInitializer;

public class ToolsMod implements ModInitializer {

    public static final String MOD_ID = "tools";

    @Override
    public void onInitialize() {
        EnchantCommand.register();
        HeldEffectListener.register();
    }
}
