/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.github.liachmodded.loquat;

import com.github.liachmodded.loquat.resource.editor.DataPackEditorServerAddon;
import com.github.liachmodded.loquat.resource.function.FunctionRecordingServerAddon;
import net.minecraft.server.MinecraftServer;

public final class LoquatConvention implements AutoCloseable {

  private final Loquat loquat;
  private final MinecraftServer server;
  private FunctionRecordingServerAddon functionRecorder;
  private DataPackEditorServerAddon dataPackEditor;

  public static LoquatConvention from(MinecraftServer server) {
    return ((LoquatServer) server).getConvention();
  }

  public LoquatConvention(Loquat loquat, MinecraftServer server) {
    this.loquat = loquat;
    this.server = server;

    init();
  }

  public Loquat getLoquat() {
    return loquat;
  }

  public MinecraftServer getServer() {
    return server;
  }

  public FunctionRecordingServerAddon getFunctionRecorder() {
    return functionRecorder;
  }

  public DataPackEditorServerAddon getDataPackEditor() {
    return dataPackEditor;
  }

  private void init() {
    functionRecorder = new FunctionRecordingServerAddon(this);
    dataPackEditor = new DataPackEditorServerAddon(this);
  }

  @Override
  public void close() {

  }
}
