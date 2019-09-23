/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.github.liachmodded.loquat.nbt;

import com.github.liachmodded.loquat.Loquat;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.command.DataCommandObject;
import net.minecraft.command.arguments.IdentifierArgumentType;
import net.minecraft.command.arguments.NbtPathArgumentType;
import net.minecraft.command.arguments.NbtPathArgumentType.NbtPath;
import net.minecraft.nbt.Tag;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.DataCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;

public final class NbtPipeline {

  private static final Operation NOOP = t -> t;
  private final DynamicCommandExceptionType incorrectNumberOfInput;
  private final DynamicCommandExceptionType invalidOperator;

  private final Loquat mod;

  private final Map<Identifier, Operation> operators;

  public NbtPipeline(Loquat mod) {
    this.mod = mod;
    this.operators = new HashMap<>();
    this.incorrectNumberOfInput = new DynamicCommandExceptionType(mod.getTextFactory()::reportWrongNumberOfInput);
    this.invalidOperator = new DynamicCommandExceptionType(mod.getTextFactory()::reportInvalidPipelineOperator);
    operators.put(Loquat.name("noop"), NOOP);

    init();
  }

  public void register(Identifier id, Operation op) {
    this.operators.put(id, op);
  }

  private void register(String name, Operation op) {
    register(Loquat.name(name), op);
  }

  private void init() {
    LiteralCommandNode<ServerCommandSource> root = CommandManager.literal("nbtpipeline")
        .then(addTargetTypes(CommandManager.argument("operation", IdentifierArgumentType.identifier()).suggests(this::suggestTransformations)))
        .build();

    mod.getCommandHandler().add(root);
  }

  private ArgumentBuilder<ServerCommandSource, ?> addTargetTypes(ArgumentBuilder<ServerCommandSource, ?> operationNode) {
    for (DataCommand.ObjectType targetType : DataCommand.TARGET_OBJECT_TYPES) {
      targetType.addArgumentsToBuilder(operationNode, b -> addTargetPath(targetType, b));
    }

    return operationNode;
  }

  private ArgumentBuilder<ServerCommandSource, ?> addSourceTypes(DataCommand.ObjectType targetType,
      ArgumentBuilder<ServerCommandSource, ?> targetPathNode) {
    for (DataCommand.ObjectType sourceType : DataCommand.SOURCE_OBJECT_TYPES) {
      sourceType.addArgumentsToBuilder(targetPathNode, b -> addSourcePath(targetType, sourceType, b));
    }

    return targetPathNode;
  }

  private ArgumentBuilder<ServerCommandSource, ?> addTargetPath(DataCommand.ObjectType targetType,
      ArgumentBuilder<ServerCommandSource, ?> targetTypeNode) {
    targetTypeNode.then(
        addSourceTypes(targetType, CommandManager.argument("targetPath", NbtPathArgumentType.nbtPath()))
    );

    return targetTypeNode;
  }

  private ArgumentBuilder<ServerCommandSource, ?> addSourcePath(DataCommand.ObjectType targetType, DataCommand.ObjectType sourceType,
      ArgumentBuilder<ServerCommandSource, ?> sourceTypeNode) {
    sourceTypeNode.then(
        CommandManager.argument("sourcePath", NbtPathArgumentType.nbtPath()).executes(ctx -> runCommand(targetType, sourceType, ctx))
    );

    return sourceTypeNode;
  }

  private int runCommand(DataCommand.ObjectType targetType, DataCommand.ObjectType sourceType, CommandContext<ServerCommandSource> context)
      throws CommandSyntaxException {
    Identifier id = IdentifierArgumentType.getIdentifier(context, "operation");
    Operation operator = operators.get(id);
    if (operator == null) {
      throw invalidOperator.create(id);
    }

    DataCommandObject target = targetType.getObject(context);
    NbtPath targetPath = NbtPathArgumentType.getNbtPath(context, "targetPath");
    DataCommandObject source = sourceType.getObject(context);
    NbtPath sourcePath = NbtPathArgumentType.getNbtPath(context, "sourcePath");

    List<Tag> inputCollection = sourcePath.get(source.getTag());
    if (inputCollection.size() != 1) {
      throw incorrectNumberOfInput.create(inputCollection.size());
    }

    Tag input = inputCollection.get(0);
    Tag output = operator.process(input); // Fail fast on command error

    int ret = targetPath.put(target.getTag(), () -> output);

    context.getSource().sendFeedback(new LiteralText("Successfully processed and set to " + ret + "targets!"), false);

    return ret;
  }

  private CompletableFuture<Suggestions> suggestTransformations(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder)
      throws CommandSyntaxException {
    return CommandSource.suggestIdentifiers(operators.keySet(), builder);
  }

  public interface Operation {

    SimpleCommandExceptionType INVALID_TAG_TYPE = new SimpleCommandExceptionType(new LiteralText("Invalid NBT type for input!"));

    Tag process(Tag input) throws CommandSyntaxException;
  }
}
