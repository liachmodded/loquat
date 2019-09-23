/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.github.liachmodded.loquat.data;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import net.minecraft.data.DataGenerator;

public final class DataDumper {

  public static void main(String... args) throws IOException {
    DataGenerator dataGenerator = new DataGenerator(new File(args[0]).toPath(), Collections.emptyList());

//        dataGenerator.install(new CartAdvancementProvider(dataGenerator));

    dataGenerator.run();
  }

}
