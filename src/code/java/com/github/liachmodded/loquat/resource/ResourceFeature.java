/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.github.liachmodded.loquat.resource;

import com.github.liachmodded.loquat.CommandHandler;
import com.github.liachmodded.loquat.Loquat;
import com.github.liachmodded.loquat.LoquatConvention;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.command.arguments.IdentifierArgumentType;
import net.minecraft.command.arguments.NbtPathArgumentType;
import net.minecraft.command.arguments.NbtPathArgumentType.NbtPath;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public final class ResourceFeature implements UseItemCallback {

  private final Loquat mod;

  private NbtPath recordingItemPath;
  private ItemStack endRecordingItem;

  public ResourceFeature(Loquat mod) {
    this.mod = mod;

    init();
  }

  private void init() {
    registerCommand();
    initFunctionRecording();
  }
  
  private void initFunctionRecording() {
    try {
      recordingItemPath = NbtPathArgumentType.nbtPath().parse(new StringReader("loquat.functionCollector"));
    } catch (CommandSyntaxException ex) {
      throw new RuntimeException(ex);
    }

    UseItemCallback.EVENT.register(this);

    endRecordingItem = new ItemStack(Items.HEART_OF_THE_SEA);
    endRecordingItem.getOrCreateSubTag("loquat").putBoolean("functionCollector", true);
  }

  private void registerCommand() {
    CommandHandler handler = mod.getCommandHandler();
    LiteralCommandNode<ServerCommandSource> root = CommandManager.literal("resource")
        .executes(handler::listSubcommands)
        .then(
            CommandManager.literal("function")
                .executes(handler::listSubcommands)
                .then(
                    CommandManager.literal("record")
                        .executes(handler::listSubcommands)
                        .then(
                            CommandManager.argument("id", IdentifierArgumentType.identifier())
                                .executes(this::startCommandRecording)
                        )
                )
        ).build();

    handler.add(root);
  }

  @Override
  public TypedActionResult<ItemStack> interact(PlayerEntity player, World world, Hand hand) {
    ItemStack stack = player.getStackInHand(hand);
    if (world.isClient) {
      return TypedActionResult.pass(stack);
    }

    if (TagEvaluator.getBoolean(recordingItemPath, stack.getTag())) {
      ResourceServerAddon addon = LoquatConvention.from(world.getServer()).getResourceFeatureServerAddon();
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

    convention.getResourceFeatureServerAddon().startCollection(player, id);

    player.inventory.insertStack(endRecordingItem.copy());

    return Command.SINGLE_SUCCESS;
  }

}
