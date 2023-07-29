package rip.alpha.core.shared.grants;

import rip.alpha.core.shared.bridge.AlphaProfileBridgeEvent;

import java.util.UUID;

public record ProfileGrantRemoveEvent(
        UUID profileID,
        Grant grant,
        Reason reason,
        boolean vanishRank,
        String removedBy,
        String removeReason) implements AlphaProfileBridgeEvent {

    public enum Reason {
        RUN_OUT,
        REPLACED,
        TAKEN
    }
}
