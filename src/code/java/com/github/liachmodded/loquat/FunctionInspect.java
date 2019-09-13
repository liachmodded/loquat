/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.github.liachmodded.loquat;

import com.github.liachmodded.loquat.mixin.CommandElementMixin;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.arguments.IdentifierArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.function.CommandFunctionManager;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

public final class FunctionInspect {
    private final CommandHandler commandHandler;
    private final Loquat loquat;
    private final DynamicCommandExceptionType invalidFunctionExceptionType;

    public FunctionInspect(Loquat loquat) {
        this.commandHandler = loquat.getCommandHandler();
        this.loquat = loquat;
        this.invalidFunctionExceptionType = new DynamicCommandExceptionType(loquat.getTextFactory()::reportInvalidFunctionId);

        init();
    }

    private void init() {
        LiteralCommandNode<ServerCommandSource> node = CommandManager.literal("funcinspect")
                .executes(commandHandler::listSubcommands)
                .then(
                        CommandManager.argument("function", IdentifierArgumentType.identifier())
                                .requires(source -> source.hasPermissionLevel(2))
                                .suggests(this::suggestFunctions)
                                .executes(this::executeInspectFunction)
                )
                .build();
        commandHandler.add(node);
    }

    private CompletableFuture<Suggestions> suggestFunctions(final CommandContext<ServerCommandSource> context, final SuggestionsBuilder builder) {
        return CommandSource
                .suggestIdentifiers(context.getSource().getMinecraftServer().getCommandFunctionManager().getFunctions().keySet(), builder);
    }

    private int executeInspectFunction(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Identifier id = IdentifierArgumentType.getIdentifier(context, "function");
        ServerCommandSource source = context.getSource();
        CommandFunctionManager functionManager = source.getMinecraftServer().getCommandFunctionManager();
        CommandFunction function = functionManager.getFunction(id).orElseThrow(() -> invalidFunctionExceptionType.create(id));

        for (CommandFunction.Element element : function.getElements()) {
            source.sendFeedback(represent(element), false);
        }

        return Command.SINGLE_SUCCESS;
    }

    private Text represent(CommandFunction.Element element) {
        if (!(element instanceof CommandFunction.CommandElement)) {
            return new LiteralText(element.toString());
        }
        ParseResults<ServerCommandSource> results = ((CommandElementMixin) element).getParsed();
        return loquat.getTextFactory().renderCommandChain(results.getReader().getString(), results.getContext().getNodes());
    }

}
