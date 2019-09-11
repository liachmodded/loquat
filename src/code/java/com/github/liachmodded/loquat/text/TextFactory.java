package com.github.liachmodded.loquat.text;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.NavigableSet;

public interface TextFactory {

    List<Formatting> ARGUMENT_FORMATS = ImmutableList.of(Formatting.AQUA, Formatting.YELLOW, Formatting.GREEN,
            Formatting.LIGHT_PURPLE, Formatting.GOLD);

    Text listSubcommands(String input, NavigableSet<CommandNode<?>> size);

    Text reportInvalidFunctionId(Object id);

    Text reportFunctionInfo(Text info);

}
