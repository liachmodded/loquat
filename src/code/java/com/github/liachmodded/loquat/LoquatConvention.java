package com.github.liachmodded.loquat;

import net.minecraft.server.MinecraftServer;

public final class LoquatConvention implements AutoCloseable {

    public static LoquatConvention from(MinecraftServer server) {
        return ((LoquatServer) server).getConvention();
    }

    private final Loquat loquat;
    private final MinecraftServer server;

    public LoquatConvention(Loquat loquat, MinecraftServer server) {
        this.loquat = loquat;
        this.server = server;
    }

    @Override
    public void close() {

    }
}
