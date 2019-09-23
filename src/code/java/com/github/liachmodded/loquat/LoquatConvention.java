/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.github.liachmodded.loquat;

import net.minecraft.server.MinecraftServer;

public final class LoquatConvention implements AutoCloseable {

  private final Loquat loquat;
  private final MinecraftServer server;
  public LoquatConvention(Loquat loquat, MinecraftServer server) {
    this.loquat = loquat;
    this.server = server;
  }

  public static LoquatConvention from(MinecraftServer server) {
    return ((LoquatServer) server).getConvention();
  }

  @Override
  public void close() {

  }
}
