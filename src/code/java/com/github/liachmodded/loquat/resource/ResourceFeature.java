/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.github.liachmodded.loquat.resource;

import static net.minecraft.server.command.CommandManager.literal;

import com.github.liachmodded.loquat.CommandHandler;
import com.github.liachmodded.loquat.Loquat;
import com.github.liachmodded.loquat.resource.editor.DataPackEditorFeature;
import com.github.liachmodded.loquat.resource.function.FunctionRecording;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.server.command.ServerCommandSource;

public final class ResourceFeature {

  private final Loquat mod;
  private final CommandHandler commandHandler;
  private final CommandNode<ServerCommandSource> commandBranch;
  private DataPackEditorFeature dataPackEditor;

  public ResourceFeature(Loquat mod) {
    this.mod = mod;
    this.commandHandler = mod.getCommandHandler();
    this.commandBranch = literal("resource")
        .executes(commandHandler::listSubcommands)
        .build();
    init();
  }

  private void init() {
    FunctionRecording functionRecording = new FunctionRecording();
    functionRecording.registerCommand(commandBranch, commandHandler);

    dataPackEditor = new DataPackEditorFeature(mod);
    dataPackEditor.registerCommands(commandBranch, commandHandler);
  }

  public DataPackEditorFeature getDataPackEditor() {
    return dataPackEditor;
  }
}
