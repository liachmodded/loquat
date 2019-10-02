/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.github.liachmodded.loquat.mixin;

import com.github.liachmodded.loquat.LoquatConvention;
import com.github.liachmodded.loquat.resource.function.FunctionRecordingServerAddon;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {

  @Shadow
  @Final
  private MinecraftServer server;
  @Shadow
  public ServerPlayerEntity player;
  private boolean collectingFunction;

  @Redirect(method = "onChatMessage(Lnet/minecraft/server/network/packet/ChatMessageC2SPacket;)V",
      at = @At(value = "INVOKE", target = "Lorg/apache/commons/lang3/StringUtils;normalizeSpace(Ljava/lang/String;)Ljava/lang/String;", remap = false))
  public String keepSpaceForChatMessage(String input) {
    return input.trim(); // don't remove space in between yet!
  }

  @ModifyArg(method = "onChatMessage(Lnet/minecraft/server/network/packet/ChatMessageC2SPacket;)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/text/TranslatableText;<init>(Ljava/lang/String;[Ljava/lang/Object;)V"),
      slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;executeCommand(Ljava/lang/String;)V"),
          to = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;broadcastChatMessage(Lnet/minecraft/text/Text;Z)V")))
  public Object[] trimChatText(Object[] params) {
    params[1] = StringUtils.normalizeSpace((String) params[1]);
    return params;
  }

  @Inject(method = "executeCommand(Ljava/lang/String;)V", at = @At("HEAD"), cancellable = true)
  public void onExecuteCommand(String command, CallbackInfo ci) {
    FunctionRecordingServerAddon addon = LoquatConvention.from(server).getFunctionRecorder();
    if (addon.isCollecting(player)) {
      addon.collectFunction(player, command);
      ci.cancel();
    }
  }
}
