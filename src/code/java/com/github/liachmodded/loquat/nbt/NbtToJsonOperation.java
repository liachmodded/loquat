package com.github.liachmodded.loquat.nbt;

import com.github.liachmodded.loquat.nbt.NbtPipeline.Operation;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.JsonOps;
import net.minecraft.datafixers.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

public final class NbtToJsonOperation implements Operation {

  @Override
  public Tag process(Tag input) throws CommandSyntaxException {
    return new StringTag(new Dynamic<>(NbtOps.INSTANCE, input).convert(JsonOps.INSTANCE).getValue().toString());
  }
}
