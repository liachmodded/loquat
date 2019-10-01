/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.github.liachmodded.loquat.resource;

import com.github.liachmodded.loquat.LoquatConvention;
import com.github.liachmodded.loquat.mixin.CommandElementMixin;
import com.github.liachmodded.loquat.text.TextFactory;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.function.CommandFunction.CommandElement;
import net.minecraft.server.function.CommandFunction.Element;
import net.minecraft.server.function.CommandFunctionManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ResourceServerAddon {

  private static final Logger LOGGER = LogManager.getLogger("Loquat resource addon");
  private final LoquatConvention convention;
  private final TextFactory textFactory;
  private final CommandFunctionManager commandFunctionManager;
  private final CommandDispatcher<ServerCommandSource> commandDispatcher;
  private final MinecraftServer server;
  private final Map<UUID, FunctionDraft> drafts = new HashMap<>();
  
  private String currentPackDraft;

  public ResourceServerAddon(LoquatConvention convention) {
    this.convention = convention;
    this.server = convention.getServer();
    this.commandFunctionManager = server.getCommandFunctionManager();
    this.commandDispatcher = server.getCommandManager().getDispatcher();
    this.textFactory = convention.getLoquat().getTextFactory();
  }

  public void collectFunction(ServerPlayerEntity player, String command) {
    try {
      CommandElement element = parseLine(command);
      drafts.get(player.getUuid()).add(element, command);
      if (element != null) {
        ParseResults<ServerCommandSource> parsed = ((CommandElementMixin) element).getParsed();
        player.sendMessage(textFactory.renderCommandChain(parsed.getReader().getString(), parsed.getContext()));
      }
    } catch (CommandSyntaxException ex) {
      player.sendMessage(textFactory.makeError(Texts.toText(ex.getRawMessage())));
    } catch (IllegalArgumentException ex) {
      player.sendMessage(textFactory.makeError(new LiteralText("Whilst parsing command: ").append(new LiteralText(ex.getMessage()))));
    }
  }

  void startCollection(ServerPlayerEntity player, Identifier id) {
    drafts.put(player.getUuid(), new FunctionDraft(id));
  }

  void endCollection(ServerPlayerEntity player) {
    if (!isCollecting(player)) {
      return;
    }
    FunctionDraft draft = drafts.remove(player.getUuid());

    CommandFunction function = new CommandFunction(draft.id, draft.elements.toArray(new Element[0]));
    TextFactory textFactory = convention.getLoquat().getTextFactory();
    for (CommandFunction.Element element : function.getElements()) {
      if (element instanceof CommandElement) {
        ParseResults<ServerCommandSource> results = ((CommandElementMixin) element).getParsed();
        Text message = textFactory.renderCommandChain(results.getReader().getString(), results.getContext());
        player.sendMessage(message);
      }
    }

    Path path = server.getFile("buildingpack").toPath();
    Path writeTo = path.resolve(ResourceType.SERVER_DATA.getName()).resolve(draft.id.getNamespace()).resolve("functions")
        .resolve(draft.id.getPath() + ".mcfunction");

    try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(writeTo))) {
      for (String line : draft.lines) {
        writer.println(line);
      }
      player.sendMessage(new LiteralText("Function written, will be effective on next reload."));
    } catch (IOException ex) {
      player.sendMessage(new LiteralText("Failed to write function! " + ex.getMessage()));
      LOGGER.error("Failed to write function {} to {}", draft.id, writeTo, ex);
    }
    // TODO actually write to a pack!
  }

  // nullable
  private CommandElement parseLine(String line) throws CommandSyntaxException {
    String string_1 = line.trim().substring(1);
    StringReader stringReader_1 = new StringReader(string_1);
    if (stringReader_1.canRead() && stringReader_1.peek() != '#') {
      if (stringReader_1.peek() == '/') {
        stringReader_1.skip();
        if (stringReader_1.peek() == '/') {
          throw new IllegalArgumentException("Unknown or invalid command '" + string_1 + "' (if you intended to make a comment, use '#' not '//')");
        }

        String string_2 = stringReader_1.readUnquotedString();
        throw new IllegalArgumentException(
            "Unknown or invalid command '" + string_1 + "' (did you mean '" + string_2 + "'? Do not use a preceding forwards slash.)");
      }

      ParseResults<ServerCommandSource> parseResults_1 = commandDispatcher.parse(stringReader_1, commandFunctionManager.getCommandFunctionSource());
      if (parseResults_1.getReader().canRead()) {
        if (parseResults_1.getExceptions().size() == 1) {
          throw parseResults_1.getExceptions().values().iterator().next();
        }

        if (parseResults_1.getContext().getRange().isEmpty()) {
          throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().createWithContext(parseResults_1.getReader());
        }

        throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext(parseResults_1.getReader());
      }

      return new CommandElement(parseResults_1);
    }
    return null;
  }

  public boolean isCollecting(ServerPlayerEntity player) {
    return drafts.containsKey(player.getUuid());
  }

  private static final class FunctionDraft {

    final Identifier id;
    final List<String> lines;
    final List<CommandElement> elements;

    FunctionDraft(Identifier id) {
      this.id = id;
      this.lines = new ArrayList<>();
      this.elements = new ArrayList<>();
    }

    void add(CommandElement element, String line) {
      if (element != null) {
        elements.add(element);
      }
      lines.add(line);
    }
  }
}
