package com.nightbeam.hallowed.config;

import com.google.gson.JsonObject;

import static com.nightbeam.hallowed.config.Json.obj;

/** {@code config/hallowed/client.json}: what you see and hear. Only read on the client. */
public final class ClientConfig {
    public final boolean skyTint;
    public final boolean fog;
    public final boolean moon;
    public final boolean ambientParticles;
    public final boolean ambientSounds;
    public final boolean vignette;
    public final float intensity;
    public final boolean banners;
    public final boolean jumpscares;
    public final boolean cameraShake;

    private ClientConfig(JsonObject o) {
        skyTint = Json.bool(o, "atmosphere.sky_tint", true);
        fog = Json.bool(o, "atmosphere.fog", true);
        moon = Json.bool(o, "atmosphere.harvest_moon", true);
        ambientParticles = Json.bool(o, "atmosphere.ambient_particles", true);
        ambientSounds = Json.bool(o, "atmosphere.ambient_sounds", true);
        vignette = Json.bool(o, "atmosphere.vignette", true);
        intensity = (float) Math.max(0, Math.min(1.5, Json.num(o, "atmosphere.intensity", 1.0)));
        banners = Json.bool(o, "effects.banners", true);
        jumpscares = Json.bool(o, "effects.jumpscares", true);
        cameraShake = Json.bool(o, "effects.camera_shake", true);
    }

    public static ClientConfig from(JsonObject json) {
        return new ClientConfig(json);
    }

    public static CommentedJson spec() {
        JsonObject d = obj(
                "atmosphere", obj("sky_tint", true, "fog", true, "harvest_moon", true, "ambient_particles", true,
                        "ambient_sounds", true, "vignette", true, "intensity", 1.0),
                "effects", obj("banners", true, "jumpscares", true, "camera_shake", true));
        return new CommentedJson(d)
                .header("Hallowed 1.0: client-side visuals and sounds. These only change what you see and hear.")
                .comment("atmosphere", "How a Hallowed Night looks: the violet sky and fog, the swollen orange Harvest Moon, drifting embers\n"
                        + "and leaves, distant whispers and bells, and a soft vignette during the Witching Hour.\n"
                        + "intensity scales all of it (0 = off, 1 = default, 1.5 = stronger).")
                .comment("effects", "banners: the dusk and midnight title cards. jumpscares: the Spook trick's flash (turn off if\n"
                        + "sudden images bother you; you still get the sound). camera_shake: the Horseman's charges and Spooked.");
    }
}
