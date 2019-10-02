/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.github.liachmodded.loquat.resource.editor;

import com.github.liachmodded.loquat.LoquatConvention;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.resource.ResourcePackContainer;
import net.minecraft.resource.ResourcePackContainer.Factory;
import net.minecraft.resource.ResourcePackCreator;
import net.minecraft.server.MinecraftServer;

public final class DataPackEditorPackProvider implements ResourcePackCreator {

  private final DataPackEditorServerAddon editorServerAddon;

  public DataPackEditorPackProvider(File worldDir, MinecraftServer server) {
    this.editorServerAddon = LoquatConvention.from(server).getDataPackEditor();
    this.editorServerAddon.worldDirectory = worldDir.toPath();
  }

  @Override
  public <T extends ResourcePackContainer> void registerContainer(Map<String, T> registry, Factory<T> factory) {
    scanPacks(editorServerAddon);

    for (Entry<String, DataPackEditor> entry : editorServerAddon.editors.entrySet()) {
      registry.put("loquat/" + entry.getKey(), entry.getValue().createProfile(factory));
    }
  }

  private void scanPacks(DataPackEditorServerAddon addon) {
    Map<String, DataPackEditor> ret = addon.editors;
    ret.clear();

    try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(addon.rootDirectory,
        path -> Files.isRegularFile(path.resolve("pack.mcmeta")))) {
      for (Path path : directoryStream) {
        String name = path.getFileName().toString();
        ret.put(name, new DataPackEditor(name, path));
      }
    } catch (IOException ex) {
      DataPackEditorServerAddon.LOGGER.error("Cannot load draft data packs!", ex);
    }
  }
}
