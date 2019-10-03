/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.github.liachmodded.loquat;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;

public final class ItemShowOff {

  private final CommandHandler commandHandler;

  public ItemShowOff(Loquat loquat) {
    this.commandHandler = loquat.getCommandHandler();

    init();
  }

  private void init() {
    LiteralCommandNode<ServerCommandSource> node = literal("showoff")
        .executes(this::executeShowoffSlot)
        .then(
            argument("slot", word())
                .suggests(this::suggestSlots)
                .executes(this::executeShowoffSlot)
        )
        .build();
    commandHandler.add(node);
  }

  private CompletableFuture<Suggestions> suggestSlots(final CommandContext<ServerCommandSource> context, final SuggestionsBuilder builder) {
    return CommandSource.suggestMatching(Arrays.stream(EquipmentSlot.values()).map(EquipmentSlot::getName).collect(Collectors.toList()), builder);
  }

  private int executeShowoffSlot(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
    EquipmentSlot slot;
    try {
      slot = EquipmentSlot.byName(getString(context, "slot").toLowerCase(Locale.ROOT));
    } catch (IllegalArgumentException ex) {
      slot = EquipmentSlot.MAINHAND;
    }
    ServerCommandSource source = context.getSource();
    Entity entity = source.getEntityOrThrow();
    if (!(entity instanceof LivingEntity)) {
      throw ServerCommandSource.REQUIRES_ENTITY_EXCEPTION.create();
    }

    LivingEntity living = (LivingEntity) entity;
    ItemStack stack = living.getEquippedStack(slot);

    if (!stack.isEmpty()) {
      source.getMinecraftServer().getPlayerManager()
          .sendToAll(new TranslatableText("chat.type.text", source.getDisplayName(), stack.toHoverableText()));
    }

    return Command.SINGLE_SUCCESS;
  }

}
