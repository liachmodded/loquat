/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.github.liachmodded.loquat.nbt;

import com.github.liachmodded.loquat.nbt.NbtPipeline.Operation;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.JsonOps;
import net.minecraft.datafixers.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

public final class JsonToNbtOperation implements Operation {

  @Override
  public Tag process(Tag input) throws CommandSyntaxException {
    JsonElement element = new Gson().fromJson(input.asString(), JsonElement.class);
    return new Dynamic<>(JsonOps.INSTANCE, element).convert(NbtOps.INSTANCE).getValue();
  }
}
