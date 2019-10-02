/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.github.liachmodded.loquat.mixin;

import com.github.liachmodded.loquat.Loquat;
import com.github.liachmodded.loquat.LoquatConvention;
import com.github.liachmodded.loquat.LoquatServer;
import com.github.liachmodded.loquat.event.PreDataPackLoadCallback;
import com.github.liachmodded.loquat.resource.editor.DataPackEditorPackProvider;
import java.io.File;
import net.minecraft.resource.ResourcePackContainer;
import net.minecraft.resource.ResourcePackContainerManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTask;
import net.minecraft.util.NonBlockingThreadExecutor;
import net.minecraft.world.level.LevelProperties;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class ServerMixin extends NonBlockingThreadExecutor<ServerTask> implements LoquatServer {

  @Shadow
  @Final
  private ResourcePackContainerManager<ResourcePackContainer> dataPackContainerManager;
  private LoquatConvention convention;

  public ServerMixin(String string_1) {
    super(string_1);
  }

  @Inject(method = "loadWorldDataPacks", at = @At("HEAD"))
  public void startWorldDataPackLoad(File file, LevelProperties lp, CallbackInfo ci) {
    PreDataPackLoadCallback.EVENT.invoker().beforeDataPackProvision((MinecraftServer) (Object) this);
  }

  @Inject(method = "loadWorldDataPacks", at = @At(value = "INVOKE", target = "Lnet/minecraft/resource/ResourcePackContainerManager;callCreators()V"))
  public void addPackProvider(File file, LevelProperties lp, CallbackInfo ci) {
    dataPackContainerManager.addCreator(new DataPackEditorPackProvider(file, (MinecraftServer) (Object) this));
  }

  @Override
  public LoquatConvention getConvention() {
    return convention;
  }

  @Override
  public LoquatConvention createConvention(Loquat mod) {
    this.convention = new LoquatConvention(mod, (MinecraftServer) (Object) this);
    return convention;
  }

  @Override
  public void clearConvention() {
    if (this.convention != null) {
      this.convention.close();
    }
    this.convention = null;
  }
}
