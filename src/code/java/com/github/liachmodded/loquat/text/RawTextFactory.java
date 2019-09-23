/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.github.liachmodded.loquat.text;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import java.util.List;
import java.util.NavigableSet;
import java.util.function.Function;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

public final class RawTextFactory implements TextFactory {

  @Override
  public <T> Text renderCommandChain(String input, T context, Function<? super T, ? extends T> childGetter,
      Function<? super T, ? extends List<? extends ParsedCommandNode<?>>> nodeGetter) {
    Text ret = new LiteralText("").formatted(Formatting.GRAY);
    int colorIndex = 0;
    int lastRangeEnd = 0;
    for (; context != null; context = childGetter.apply(context)) {
      for (ParsedCommandNode<?> each : nodeGetter.apply(context)) {
        StringRange range = each.getRange();
        if (lastRangeEnd < range.getStart()) {
          ret.append(input.substring(lastRangeEnd, range.getStart()));
        }
        lastRangeEnd = range.getEnd();
        Text text = new LiteralText(range.get(input));
        if (each.getNode() instanceof ArgumentCommandNode) {
          text.formatted(TextFactory.getFormatting(colorIndex));
          colorIndex++;
        }
        text.styled(style -> {
          String partialCommand = input.substring(0, range.getEnd());
          style.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, partialCommand));
          style.setInsertion(partialCommand);
        });
        ret.append(text);

      }
    }
    return ret;
  }

  @Override
  public Text listSubcommands(CommandContext<ServerCommandSource> context, NavigableSet<CommandNode<ServerCommandSource>> options) {
    String input = context.getInput();
    Text ret = new LiteralText("Subcommands for ")
        .append(renderCommandChain(input, context))
        .formatted(Formatting.GRAY).append(":");
    for (CommandNode<ServerCommandSource> node : options) {
      if (!node.getRequirement().test(context.getSource())) {
        continue;
      }
      if (node instanceof ArgumentCommandNode) {
        Suggestions suggestions;
        try {
          suggestions = node.listSuggestions(context, new SuggestionsBuilder(input, input.length())).getNow(null);
        } catch (CommandSyntaxException ex) {
          suggestions = null;
        }
        if (suggestions == null || suggestions.isEmpty()) {
          Text nodeName = new LiteralText(node.getUsageText());
          ret.append("\n").append(nodeName);
          continue;
        }
        for (Suggestion suggestion : suggestions.getList()) {
          Text argumentSuggestion = new LiteralText(suggestion.getText());
          argumentSuggestion.styled(style -> {
            style.setColor(Formatting.GRAY);
            style.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, input + " "));
            if (suggestion.getTooltip() != null) {
              style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Texts.toText(suggestion.getTooltip())));
            }
          });
          ret.append("\n").append(argumentSuggestion);
        }
      } else {
        Text nodeName = new LiteralText(node.getUsageText());
        nodeName.styled(style -> {
          style.setColor(Formatting.GRAY);
          style.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, input + " " + node.getName()));
        });
        ret.append("\n").append(nodeName);
      }
    }
    return ret;
  }

  @Override
  public Text reportInvalidSlotName(Object id) {
    return new LiteralText("Invalid slot name \"").append(new LiteralText(id.toString()).formatted(Formatting.GRAY)).append("\"!");
  }

  @Override
  public Text reportInvalidFunctionId(Object id) {
    return new LiteralText("Invalid function id \"").append(new LiteralText(id.toString()).formatted(Formatting.GRAY)).append("\"!");
  }

  @Override
  public Text reportFunctionInfo(Text info) {
    return info;
  }

  @Override
  public Text nextPage() {
    return new TranslatableText("spectatorMenu.next_page");
  }

  @Override
  public Text previousPage() {
    return new TranslatableText("spectatorMenu.previous_page");
  }

  @Override
  public Text reportWrongNumberOfInput(Object count) {
    return new LiteralText("Wrong number of input \"").append(new LiteralText(count.toString()).formatted(Formatting.GRAY)).append("\"!");
  }

  @Override
  public Text reportInvalidPipelineOperator(Object id) {
    return new LiteralText("Invalid nbt operator id \"").append(new LiteralText(id.toString()).formatted(Formatting.GRAY)).append("\"!");
  }
}
