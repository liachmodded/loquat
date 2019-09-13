/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.github.liachmodded.loquat.text;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.NavigableSet;

public interface TextFactory {

    List<Formatting> ARGUMENT_FORMATS = ImmutableList.of(Formatting.AQUA, Formatting.YELLOW, Formatting.GREEN,
            Formatting.LIGHT_PURPLE, Formatting.GOLD);

    static Formatting getFormatting(int index) {
        return ARGUMENT_FORMATS.get(index % ARGUMENT_FORMATS.size());
    }

    Text renderCommandChain(String input, List<? extends ParsedCommandNode<?>> context);

    Text listSubcommands(CommandContext<ServerCommandSource> context, NavigableSet<CommandNode<ServerCommandSource>> nodes);

    Text reportInvalidFunctionId(Object id);

    Text reportFunctionInfo(Text info);

    Text nextPage();

    Text previousPage();

}
