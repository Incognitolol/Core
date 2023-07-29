package rip.alpha.core.shared.server.event;

import rip.alpha.core.shared.bridge.AlphaBridgeEvent;

import java.util.UUID;

public record NetworkServerSendPlayerEvent(UUID playerId, String serverTo) implements AlphaBridgeEvent {
}
