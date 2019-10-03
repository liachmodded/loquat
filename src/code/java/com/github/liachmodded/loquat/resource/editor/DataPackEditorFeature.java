/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.github.liachmodded.loquat.resource.editor;

import static com.github.liachmodded.loquat.resource.editor.DataPackEditorServerAddon.getServerAddon;
import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import com.github.liachmodded.loquat.CommandHandler;
import com.github.liachmodded.loquat.Loquat;
import com.github.liachmodded.loquat.text.TextFactory;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;

public final class DataPackEditorFeature {

  private final DynamicCommandExceptionType existingPack;
  private final DynamicCommandExceptionType nonExistentPack;
  private final SimpleCommandExceptionType noPackChosen;
  private final TextFactory textFactory;
  Path editorRoot;

  public DataPackEditorFeature(Loquat mod) {
    this.editorRoot = mod.getModLoader().getConfigDirectory().toPath().resolve("datapackedit");

    this.textFactory = mod.getTextFactory();
    this.existingPack = new DynamicCommandExceptionType(textFactory::reportExistingEditorName);
    this.nonExistentPack = new DynamicCommandExceptionType(textFactory::reportExistingEditorName);
    this.noPackChosen = new SimpleCommandExceptionType(textFactory.reportNoChosenEditor());
  }

  public void registerCommands(CommandNode<ServerCommandSource> node, CommandHandler handler) {
    CommandNode<ServerCommandSource> branch = literal("editor")
        .executes(handler::listSubcommands)
        .then(
            literal("create")
                .executes(handler::listSubcommands)
                .then(
                    argument("name", string())
                        .executes(this::createEditor)
                )
        )
        .then(
            literal("choose")
                .executes(handler::listSubcommands)
                .then(
                    argument("name", string())
                        .suggests(this::suggestPackNames)
                        .executes(this::chooseEditor)
                )
        )
        .then(
            literal("discard")
                .executes(this::discardCurrentEditor)
                .then(
                    argument("name", string())
                        .suggests(this::suggestPackNames)
                        .executes(this::discardEditor)
                )
        )
        .build();

    node.addChild(branch);
  }

  private int createEditor(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
    String packName = getString(context, "name");
    DataPackEditorServerAddon addon = getServerAddon(context);

    if (addon.editors.containsKey(packName)) {
      throw existingPack.create(packName);
    }

    addon.create(packName, editorRoot);
    return SINGLE_SUCCESS;
  }

  private CompletableFuture<Suggestions> suggestPackNames(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
    DataPackEditorServerAddon addon = getServerAddon(context);
    return CommandSource.suggestMatching(addon.editors.keySet(), builder);
  }

  private int chooseEditor(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
    String packName = getString(context, "name");
    DataPackEditorServerAddon addon = getServerAddon(context);

    DataPackEditor editor = addon.editors.get(packName);
    if (editor == null) {
      throw nonExistentPack.create(packName);
    }

    addon.activeEditor = editor;

    return SINGLE_SUCCESS;
  }

  private int discardCurrentEditor(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
    DataPackEditorServerAddon addon = getServerAddon(context);
    DataPackEditor target = addon.activeEditor;

    if (target == null) {
      throw noPackChosen.create();
    }

    addon.activeEditor = null;
    addon.editors.remove(target.getName());

    return SINGLE_SUCCESS;
  }

  private int discardEditor(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
    String packName = getString(context, "name");
    DataPackEditorServerAddon addon = getServerAddon(context);

    DataPackEditor removed = addon.editors.remove(packName);
    if (removed == null) {
      throw nonExistentPack.create(packName);
    }

    if (removed == addon.activeEditor) {
      addon.activeEditor = null;
    }

    return SINGLE_SUCCESS;
  }

}
