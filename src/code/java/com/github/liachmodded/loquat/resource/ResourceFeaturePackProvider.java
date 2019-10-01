/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.github.liachmodded.loquat.resource;

import java.io.File;
import java.util.Map;
import net.minecraft.resource.DirectoryResourcePack;
import net.minecraft.resource.ResourcePackContainer;
import net.minecraft.resource.ResourcePackContainer.Factory;
import net.minecraft.resource.ResourcePackContainer.InsertionPosition;
import net.minecraft.resource.ResourcePackCreator;
import net.minecraft.resource.metadata.PackResourceMetadata;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.LiteralText;

public class ResourceFeaturePackProvider implements ResourcePackCreator {
  
  private final MinecraftServer server;
  
  public ResourceFeaturePackProvider(MinecraftServer server) {
    this.server = server;
  }
  
  @Override
  public <T extends ResourcePackContainer> void registerContainer(Map<String, T> registry, Factory<T> factory) {
    registry.put("loquat/apple", factory.create("loquat/apple", false, () -> new DirectoryResourcePack(new File(server.getRunDirectory(), "buildingpack")), new DirectoryResourcePack(new File(server.getRunDirectory(), "buildingpack")), new PackResourceMetadata(new LiteralText("apple"), 4), InsertionPosition.TOP));
  }
}
