/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.github.liachmodded.loquat.mixin;

import com.github.liachmodded.loquat.Loquat;
import com.github.liachmodded.loquat.LoquatConvention;
import com.github.liachmodded.loquat.LoquatServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTask;
import net.minecraft.util.NonBlockingThreadExecutor;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MinecraftServer.class)
public abstract class ServerMixin extends NonBlockingThreadExecutor<ServerTask> implements LoquatServer {

  private LoquatConvention convention;

  public ServerMixin(String string_1) {
    super(string_1);
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
