package rip.alpha.core.shared.grants;

import rip.alpha.core.shared.bridge.AlphaProfileBridgeEvent;

import java.util.UUID;

public record ProfileGrantAddEvent(UUID profileID, Grant grant, boolean initsRank) implements AlphaProfileBridgeEvent {

}
