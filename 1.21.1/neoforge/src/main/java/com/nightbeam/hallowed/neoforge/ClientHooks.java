package com.nightbeam.hallowed.neoforge;

import com.nightbeam.hallowed.client.HallowedClient;
import com.nightbeam.hallowed.network.FxPayload;
import com.nightbeam.hallowed.network.NightPayload;

/** Client-only payload handling, kept in its own class so a dedicated server never loads the client classes. */
final class ClientHooks {
    private ClientHooks() {
    }

    static void night(NightPayload payload) {
        HallowedClient.handleNight(payload);
    }

    static void fx(FxPayload payload) {
        HallowedClient.handleFx(payload);
    }
}
