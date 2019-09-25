/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.github.liachmodded.loquat.resource;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.function.Supplier;
import java.util.stream.Collector;
import net.minecraft.command.arguments.NbtPathArgumentType.NbtPath;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.Tag;
import org.apache.commons.lang3.mutable.MutableBoolean;

public final class TagEvaluator {

  public static boolean getBoolean(NbtPath path, Tag root) {
    return eval(path, root, () -> Boolean.FALSE, Collector.of(MutableBoolean::new, (bool, tag) -> {
      if (tag instanceof ByteTag && ((ByteTag) tag).getByte() > 0) {
        bool.setTrue();
      }
    }, (b1, b2) -> {
      if (b2.booleanValue())
        b1.setTrue();
      return b1;
    }, MutableBoolean::booleanValue));
  }

  public static <T> T eval(NbtPath path, Tag root, Supplier<T> fallback, Collector<Tag, ?, T> collector) {
    if (root == null)
      return fallback.get();
    try {
      return path.get(root).stream().collect(collector);
    } catch (CommandSyntaxException ex) {
      return fallback.get();
    }
  }

  private TagEvaluator() {}
}
