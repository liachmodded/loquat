/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.github.liachmodded.loquat;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.SystemUtil;

import java.util.TreeSet;

public final class CommandHandler {

    private final Loquat mod;
    private final LiteralCommandNode<ServerCommandSource> root;

    public CommandHandler(Loquat mod) {
        this.mod = mod;
        this.root = LiteralArgumentBuilder.<ServerCommandSource>literal(Loquat.ID)
                .executes(this::listSubcommands)
                .build();
    }

    /**
     * Add a subcommand tree to the root command.
     *
     * @param command the subcommand
     */
    public void add(CommandNode<ServerCommandSource> command) {
        this.root.addChild(command);
    }

    /**
     * Utility method for execution of tree branch nodes.
     *
     * <p>Prints the available sub commands from the executed command node.
     *
     * @param context the command context
     * @see Command
     * @return the command output
     */
    public int listSubcommands(CommandContext<ServerCommandSource> context) {
        CommandNode<ServerCommandSource> owner = SystemUtil.getLast(context.getNodes()).getNode();

        Text reply = mod.getTextFactory().listSubcommands(context, new TreeSet<>(owner.getChildren()));

        context.getSource().sendFeedback(reply, false);

        return Command.SINGLE_SUCCESS;
    }

    /**
     * Registers the root command.
     *
     * @param dispatcher the server command dispatcher
     */
    void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.getRoot().addChild(root);
    }

}
