package com.nightbeam.hallowed;

import com.nightbeam.hallowed.config.Configs;
import com.nightbeam.hallowed.content.ModContent;
import com.nightbeam.hallowed.platform.Platform;
import com.nightbeam.hallowed.platform.Registrar;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Hallowed: Halloween nights, trick-or-treating and the Hollow Horseman. */
public final class Hallowed {
    public static final String MOD_ID = "hallowed";
    public static final String NAME = "Hallowed";
    public static final Logger LOG = LoggerFactory.getLogger(NAME);

    private static Platform platform;

    private Hallowed() {
    }

    /** Called by each loader's entry point before anything else. */
    public static void init(Platform platform, Registrar registrar) {
        Hallowed.platform = platform;
        Configs.load(platform.configDir(), platform.isClient());
        ModContent.register(registrar);
    }

    public static Platform platform() {
        return platform;
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
