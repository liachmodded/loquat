package com.github.liachmodded.loquat.mixin;

import com.mojang.brigadier.ParseResults;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CommandFunction.CommandElement.class)
public interface CommandElementMixin {

    @Accessor ParseResults<ServerCommandSource> getParsed();

}
