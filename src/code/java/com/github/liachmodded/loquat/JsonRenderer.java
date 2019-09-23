/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.github.liachmodded.loquat;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.command.arguments.IdentifierArgumentType;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;

public final class JsonRenderer {

  private final CommandHandler commandHandler;
  private final Collection<String> dataTypes;
  private final DynamicCommandExceptionType ioExceptionType = new DynamicCommandExceptionType(
      ioe -> new LiteralText(((IOException) ioe).getMessage()));

  public JsonRenderer(Loquat mod) {
    commandHandler = mod.getCommandHandler();
    dataTypes = Lists.newArrayList("tags", "recipes", "advancements");

    init();
  }

  void init() {

  }

  private void registerCommand() {
    CommandNode<ServerCommandSource> modifyDataType = CommandManager.literal("json-data-type")
        .then(
            CommandManager.literal("add")
                .then(
                    CommandManager.argument("data-type", StringArgumentType.word())
                        .executes(this::addJsonDataType)
                        .build()
                )
                .build()
        )
        .then(
            CommandManager.literal("remove")
                .then(
                    CommandManager.argument("data-type", StringArgumentType.word())
                        .executes(this::removeJsonDataType)
                        .build()
                )
                .build()
        )
        .build();

    CommandNode<ServerCommandSource> node = CommandManager.literal("json")
        .then(
            CommandManager.argument("location", IdentifierArgumentType.identifier())
                .suggests(this::suggestResources)
        )
        .executes(this::executeJson)
        .build();
  }

  private int removeJsonDataType(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
    String st = StringArgumentType.getString(context, "data-type");
    if (st.indexOf(':') != -1 || Identifier.tryParse(st) == null) {
      throw IdentifierArgumentType.UNKNOWN_EXCEPTION.create(st);
    }

    return dataTypes.remove(st) ? SINGLE_SUCCESS : 0;
  }

  private int addJsonDataType(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
    String st = StringArgumentType.getString(context, "data-type");
    if (st.indexOf(':') != -1 || Identifier.tryParse(st) == null) {
      throw IdentifierArgumentType.UNKNOWN_EXCEPTION.create(st);
    }

    dataTypes.add(st);
    return SINGLE_SUCCESS;
  }

  private CompletableFuture<Suggestions> suggestResources(final CommandContext<ServerCommandSource> context, final SuggestionsBuilder builder) {
    ResourceManager manager = context.getSource().getMinecraftServer().getDataManager();
    Collection<String> dataTypes = new ArrayList<>(this.dataTypes); // copy to prevent async issues
    return CompletableFuture.supplyAsync(() -> {
      for (String dataType : dataTypes) {
        for (Identifier each : manager.findResources(dataType, name -> name.endsWith(".json"))) {
          builder.suggest(each.toString());
        }
      }
      return builder.build();
    });
  }

  private int executeJson(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
    Identifier id = IdentifierArgumentType.getIdentifier(context, "location");
    ServerCommandSource source = context.getSource();
    try (Resource resource = source.getMinecraftServer().getDataManager().getResource(id)) {
      JsonElement jsonElement = new Gson().fromJson(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8), JsonElement.class);
      jsonElement.getAsString();
    } catch (IOException ex) {
      throw ioExceptionType.create(ex);
    }

    return SINGLE_SUCCESS;
  }

}
