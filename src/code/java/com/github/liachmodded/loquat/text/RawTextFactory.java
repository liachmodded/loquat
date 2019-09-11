package com.github.liachmodded.loquat.text;

import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.NavigableSet;
import java.util.concurrent.atomic.AtomicInteger;

public final class RawTextFactory implements TextFactory {

    @Override
    public Text listSubcommands(String input, NavigableSet<CommandNode<?>> options) {
        Text ret = new LiteralText("Subcommands for ").append(new LiteralText(input).formatted(Formatting.GRAY)).append(":");
        AtomicInteger colorIndex = new AtomicInteger(0);
        for (CommandNode<?> node : options) {
            Text nodeName = new LiteralText(node.getUsageText());
            nodeName.styled(style -> {
                if (node instanceof ArgumentCommandNode) {
                    style.setColor(TextFactory.ARGUMENT_FORMATS.get(colorIndex.getAndIncrement() % TextFactory.ARGUMENT_FORMATS.size()));
                    style.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, input + " "));
                } else {
                    style.setColor(Formatting.GRAY);
                    style.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, input + " " + node.getName()));
                }
            });
            ret.append("\n").append(nodeName);
        }
        return ret;
    }

    @Override
    public Text reportInvalidFunctionId(Object id) {
        return new LiteralText("Invalid function id \"").append(new LiteralText(id.toString()).formatted(Formatting.GRAY)).append("\"!");
    }

    @Override
    public Text reportFunctionInfo(Text info) {
        return info;
    }
}
