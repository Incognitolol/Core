package rip.alpha.core.shared.server.event;

import rip.alpha.core.shared.bridge.AlphaBridgeEvent;

public record NetworkServerSendServerEvent(String serverFrom, String serverTo) implements AlphaBridgeEvent {
}
