package com.github.liachmodded.loquat;

import com.github.liachmodded.loquat.mixin.CommandElementMixin;
import com.github.liachmodded.loquat.text.TextFactory;
import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.arguments.IdentifierArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.function.CommandFunctionManager;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class FunctionInspect {
    private final CommandHandler commandHandler;
    private final DynamicCommandExceptionType invalidFunctionExceptionType;

    public FunctionInspect(Loquat loquat) {
        this.commandHandler = loquat.getCommandHandler();
        this.invalidFunctionExceptionType = new DynamicCommandExceptionType(loquat.getTextFactory()::reportInvalidFunctionId);

        init();
    }

    private void init() {
        LiteralCommandNode<ServerCommandSource> node = CommandManager.literal("funcinspect")
                .executes(commandHandler::listSubcommands)
                .then(
                        CommandManager.argument("function", IdentifierArgumentType.identifier())
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
        ImmutableStringReader reader = results.getReader();
        Text ret = new LiteralText("").formatted(Formatting.GRAY);
        int colorIndex = 0;
        int lastRangeEnd = 0;
        for (ParsedCommandNode<ServerCommandSource> each : results.getContext().getNodes()) {
            StringRange range = each.getRange();
            if (lastRangeEnd < range.getStart()) {
                ret.append(reader.getString().substring(lastRangeEnd, range.getStart()));
            }
            Text text = new LiteralText(range.get(reader));
            if (each.getNode() instanceof ArgumentCommandNode) {
                text.formatted(TextFactory.ARGUMENT_FORMATS.get(colorIndex));
                colorIndex++;
                if (colorIndex >= TextFactory.ARGUMENT_FORMATS.size()) {
                    colorIndex %= TextFactory.ARGUMENT_FORMATS.size();
                }
            }
            ret.append(text);
            lastRangeEnd = range.getEnd();
        }
        return ret;
    }

}
