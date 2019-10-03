/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.github.liachmodded.loquat.resource.editor;

import com.github.liachmodded.loquat.LoquatConvention;
import com.mojang.brigadier.context.CommandContext;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.server.command.ServerCommandSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class DataPackEditorServerAddon {

  static final Logger LOGGER = LogManager.getLogger("Data Pack Editor");
  private final LoquatConvention convention;
  final Map<String, DataPackEditor> editors;
  final Path rootDirectory;
  @MonotonicNonNull Path worldDirectory;
  @Nullable DataPackEditor activeEditor;

  public DataPackEditorServerAddon(LoquatConvention convention) {
    this.convention = convention;
    this.editors = new HashMap<>();
    this.rootDirectory = convention.getLoquat().getResourceFeature().getDataPackEditor().editorRoot;
  }

  public static DataPackEditorServerAddon getServerAddon(CommandContext<ServerCommandSource> context) {
    return LoquatConvention.from(context.getSource().getMinecraftServer()).getDataPackEditor();
  }

  DataPackEditor create(String name, Path rootPath) {
    DataPackEditor ret = new DataPackEditor(name, rootPath.resolve(name));
    editors.put(name, ret);
    return ret;
  }

}
