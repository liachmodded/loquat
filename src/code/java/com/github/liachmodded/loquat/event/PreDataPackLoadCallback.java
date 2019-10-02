/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.github.liachmodded.loquat.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.MinecraftServer;

public interface PreDataPackLoadCallback {
  
  Event<PreDataPackLoadCallback> EVENT = EventFactory.createArrayBacked(PreDataPackLoadCallback.class, callbacks -> server -> {
    for (PreDataPackLoadCallback callback : callbacks) {
      callback.beforeDataPackProvision(server);
    }
  });
  
  void beforeDataPackProvision(MinecraftServer server);

}
