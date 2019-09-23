/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.github.liachmodded.loquat.nbt;

import com.github.liachmodded.loquat.nbt.NbtPipeline.Operation;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.TagHelper;

public final class UuidCompoundToStringOperation implements Operation {

  @Override
  public Tag process(Tag input) throws CommandSyntaxException {
    if (!(input instanceof CompoundTag)) {
      throw INVALID_TAG_TYPE.create();
    }

    UUID result = TagHelper.deserializeUuid((CompoundTag) input);
    return new StringTag(result.toString());
  }
}
