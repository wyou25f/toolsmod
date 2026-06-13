package com.toolsmod;

import com.toolsmod.commands.EnchantCommand;
import com.toolsmod.listener.HeldEffectListener;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ToolsMod implements ModInitializer {

    public static final String MOD_ID = "toolsplus";

    @Override
    public void onInitialize() {
        EnchantCommand.register();
        HeldEffectListener.register();

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            Path root = server.getServerDirectory().resolve("toolsplus");
            try {
                Files.createDirectories(root.resolve("music_input"));
                Files.createDirectories(root.resolve("resourcepack"));
            } catch (IOException ignored) {}
        });
    }
}
