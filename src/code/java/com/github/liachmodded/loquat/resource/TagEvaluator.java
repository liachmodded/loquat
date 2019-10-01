/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.github.liachmodded.loquat.resource;

import com.google.common.collect.Iterables;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import java.util.NoSuchElementException;
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
      if (b2.booleanValue()) {
        b1.setTrue();
      }
      return b1;
    }, MutableBoolean::getValue));
  }

  public static <T> T getFirst(NbtPath path, Tag root, Supplier<T> fallback, Converter<T> converter) {
    if (root == null) {
      return fallback.get();
    }
    try {
      List<Tag> tags = path.get(root);
      if (tags.isEmpty()) {
        return fallback.get();
      }
      T result = converter.convert(tags.get(0));
      return result == null ? fallback.get() : result;
    } catch (CommandSyntaxException | IllegalArgumentException ex) {
      return fallback.get();
    }
  }

  public static <T> T getLast(NbtPath path, Tag root, Supplier<T> fallback, Converter<T> converter) {
    if (root == null) {
      return fallback.get();
    }
    try {
      T result = converter.convert(Iterables.getLast(path.get(root)));
      return result == null ? fallback.get() : result;
    } catch (CommandSyntaxException | IllegalArgumentException | NoSuchElementException ex) {
      return fallback.get();
    }
  }

  public static <T> T eval(NbtPath path, Tag root, Supplier<T> fallback, Collector<Tag, ?, T> collector) {
    if (root == null) {
      return fallback.get();
    }
    try {
      return path.get(root).stream().collect(collector);
    } catch (CommandSyntaxException ex) {
      return fallback.get();
    }
  }

  public interface Converter<T> {

    T convert(Tag tag) throws IllegalArgumentException;
  }

  private TagEvaluator() {}
}
