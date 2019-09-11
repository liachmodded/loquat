package com.github.liachmodded.loquat;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.client.network.packet.InventoryS2CPacket;
import net.minecraft.client.network.packet.OpenWrittenBookS2CPacket;
import net.minecraft.server.command.ServerCommandSource;

public final class JsonRenderer {

    private final CommandHandler commandHandler;

    public JsonRenderer(Loquat mod) {
        commandHandler = mod.getCommandHandler();

        init();
    }

    void init() {

    }

    private void registerCommand() {
        CommandNode<ServerCommandSource> node = LiteralArgumentBuilder.<ServerCommandSource>literal("json")
                .executes(this::executeJson)
                .build();
    }

    private int executeJson(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        source.getMinecraftServer().getFile("");

        source.getPlayer().networkHandler.sendPacket(new InventoryS2CPacket());
        source.getPlayer().networkHandler.sendPacket(new OpenWrittenBookS2CPacket());

        return Command.SINGLE_SUCCESS;
    }

}
