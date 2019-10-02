/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.github.liachmodded.loquat.resource.editor;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import com.github.liachmodded.loquat.CommandHandler;
import com.github.liachmodded.loquat.Loquat;
import com.github.liachmodded.loquat.LoquatConvention;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import java.nio.file.Path;
import net.minecraft.server.command.ServerCommandSource;

public final class DataPackEditorFeature {

  Path editorRoot;

  public DataPackEditorFeature(Loquat mod) {
    this.editorRoot = mod.getModLoader().getConfigDirectory().toPath().resolve("datapackedit");
  }

  public void registerCommands(CommandNode<ServerCommandSource> node, CommandHandler handler) {
    CommandNode<ServerCommandSource> branch = literal("editor")
        .executes(handler::listSubcommands)
        .then(
            literal("create")
                .executes(handler::listSubcommands)
                .then(
                    argument("name", StringArgumentType.string())
                        .executes(this::createEditor)
                )
        )
        .then(
            literal("choose")
                .executes(handler::listSubcommands)
        )
        .build();

    node.addChild(branch);
  }

  private int createEditor(CommandContext<ServerCommandSource> context) {
    String packName = StringArgumentType.getString(context, "name");
    LoquatConvention convention = LoquatConvention.from(context.getSource().getMinecraftServer());

    convention.getDataPackEditor().create(packName, editorRoot);
    return SINGLE_SUCCESS;
  }

}
