/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.github.liachmodded.loquat.resource.function;

import static net.minecraft.command.arguments.IdentifierArgumentType.identifier;
import static net.minecraft.command.arguments.NbtPathArgumentType.nbtPath;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import com.github.liachmodded.loquat.CommandHandler;
import com.github.liachmodded.loquat.LoquatConvention;
import com.github.liachmodded.loquat.nbt.TagEvaluator;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.command.arguments.IdentifierArgumentType;
import net.minecraft.command.arguments.NbtPathArgumentType.NbtPath;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public final class FunctionRecording implements UseItemCallback {

  private final NbtPath recordingItemPath;
  private final ItemStack endRecordingItem;

  public FunctionRecording() {
    try {
      recordingItemPath = nbtPath().parse(new StringReader("loquat.functionCollector"));
    } catch (CommandSyntaxException ex) {
      throw new RuntimeException(ex);
    }

    UseItemCallback.EVENT.register(this);

    endRecordingItem = new ItemStack(Items.HEART_OF_THE_SEA);
    endRecordingItem.getOrCreateSubTag("loquat").putBoolean("functionCollector", true);
  }

  public void registerCommand(CommandNode<ServerCommandSource> node, CommandHandler handler) {
    LiteralCommandNode<ServerCommandSource> branch =
        literal("function")
            .executes(handler::listSubcommands)
            .then(
                literal("record")
                    .executes(handler::listSubcommands)
                    .then(
                        argument("id", identifier())
                            .executes(this::startCommandRecording)
                    )
            ).build();

    node.addChild(branch);
  }

  @Override
  public TypedActionResult<ItemStack> interact(PlayerEntity player, World world, Hand hand) {
    ItemStack stack = player.getStackInHand(hand);
    if (world.isClient) {
      return TypedActionResult.pass(stack);
    }

    if (TagEvaluator.getBoolean(recordingItemPath, stack.getTag())) {
      FunctionRecordingServerAddon addon = LoquatConvention.from(world.getServer()).getFunctionRecorder();
      addon.endCollection((ServerPlayerEntity) player);
      stack.decrement(1);
      return TypedActionResult.successWithSwing(stack);
    }

    return TypedActionResult.pass(stack);
  }

  private int startCommandRecording(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
    Identifier id = IdentifierArgumentType.getIdentifier(context, "id");
    LoquatConvention convention = LoquatConvention.from(context.getSource().getMinecraftServer());
    ServerPlayerEntity player = context.getSource().getPlayer();

    convention.getFunctionRecorder().startCollection(player, id);

    player.inventory.insertStack(endRecordingItem.copy());

    return Command.SINGLE_SUCCESS;
  }

}
