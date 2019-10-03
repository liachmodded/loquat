/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.github.liachmodded.loquat.resource.editor;

import java.nio.file.Path;
import net.minecraft.resource.DirectoryResourcePack;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourcePackContainer;
import net.minecraft.resource.ResourcePackContainer.InsertionPosition;
import net.minecraft.resource.metadata.PackResourceMetadata;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public final class DataPackEditor {
  
  private String name;
  private Path path;
  private Text description;
  
  DataPackEditor(String name, Path path) {
    this.name = name;
    this.path = path;
    this.description = new LiteralText("");
  }

  public String getName() {
    return name;
  }

  public void setDescription(Text text) {
    this.description = text;
  }

  public Text getDescription() {
    return description;
  }

  <T extends ResourcePackContainer> T createProfile(ResourcePackContainer.Factory<T> factory) {
    return factory.create("loquat/" + name, false, this::makePack, this.makePack(), new PackResourceMetadata(description, 4), InsertionPosition.TOP);
  }
  
  private ResourcePack makePack() {
    return new DirectoryResourcePack(path.toFile());
  }
}
