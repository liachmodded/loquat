/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.github.liachmodded.loquat.nbt;

import com.github.liachmodded.loquat.nbt.NbtPipeline.Operation;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.UUID;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.text.LiteralText;

public final class UuidStringToCompoundOperation implements Operation {

  private static final DynamicCommandExceptionType INVALID_UUID = new DynamicCommandExceptionType(s -> new LiteralText("Invalid UUID String " + s +
      " for input!"));

  @Override
  public Tag process(Tag input) throws CommandSyntaxException {
    if (!(input instanceof StringTag)) {
      throw INVALID_TAG_TYPE.create();
    }

    String content = input.asString();
    try {
      UUID uuid = UUID.fromString(content);
      return NbtHelper.fromUuid(uuid);
    } catch (IllegalArgumentException ex) {
      throw INVALID_UUID.create(content);
    }
  }
}
