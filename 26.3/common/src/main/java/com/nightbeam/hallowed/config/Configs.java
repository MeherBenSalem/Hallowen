package com.nightbeam.hallowed.config;

import com.google.gson.JsonObject;
import com.nightbeam.hallowed.Hallowed;
import java.nio.file.Path;

/** Loads and holds the files under {@code config/hallowed/}. */
public final class Configs {
    private static HallowedConfig common = HallowedConfig.from(new JsonObject());
    private static ClientConfig client = ClientConfig.from(new JsonObject());
    private static Path dir = Path.of("config", Hallowed.MOD_ID);

    private Configs() {
    }

    public static HallowedConfig common() {
        return common;
    }

    public static ClientConfig client() {
        return client;
    }

    /** Loads (or creates) every file. Safe to call again to reload. */
    public static synchronized void load(Path configRoot, boolean client) {
        dir = configRoot.resolve(Hallowed.MOD_ID);
        common = HallowedConfig.from(HallowedConfig.spec().load(dir.resolve("hallowed.json")));
        if (client) {
            Configs.client = ClientConfig.from(ClientConfig.spec().load(dir.resolve("client.json")));
        }
    }
}
