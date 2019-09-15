/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.github.liachmodded.loquat;

import com.github.liachmodded.loquat.text.RawTextFactory;
import com.github.liachmodded.loquat.text.TextFactory;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.server.ServerStartCallback;
import net.fabricmc.fabric.api.event.server.ServerStopCallback;
import net.fabricmc.fabric.api.registry.CommandRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

/**
 * Main mod class.
 */
public final class Loquat implements ModInitializer {

    /** The mod identifier; the namespace used by loquat mod. */
    public static final String ID = "loquat";

    private FabricLoader modLoader;
    private ModContainer modContainer;
    private TextFactory textFactory;
    private CommandHandler commandHandler;

    /** Convenience method to create a identifier for loquat. */
    public static Identifier name(String name) {
        return new Identifier(ID, name);
    }

    public FabricLoader getModLoader() {
        return modLoader;
    }

    public ModContainer getModContainer() {
        return modContainer;
    }

    /**
     * Gets the text factory for localizable messages.
     *
     * @return the text factory
     */
    public TextFactory getTextFactory() {
        return textFactory;
    }

    /**
     * Gets the command handler.
     *
     * @return the command handler
     */
    public CommandHandler getCommandHandler() {
        return commandHandler;
    }

    @Override
    public void onInitialize() {
        this.modLoader = FabricLoader.getInstance();
        this.modContainer = modLoader.getModContainer(ID).orElseThrow(() -> new IllegalStateException("loquat not loaded!"));
        this.textFactory = new RawTextFactory();
        this.commandHandler = new CommandHandler(this);
        CommandRegistry.INSTANCE.register(false, commandHandler::registerCommands);

        ServerStartCallback.EVENT.register(this::onServerStart);
        ServerStopCallback.EVENT.register(this::onServerStop);
        new FunctionInspect(this);
        new ItemShowOff(this);
    }

    private void onServerStart(MinecraftServer server) {
        ((LoquatServer) server).createConvention(this);
    }

    private void onServerStop(MinecraftServer server) {
        ((LoquatServer) server).clearConvention();
    }


}
