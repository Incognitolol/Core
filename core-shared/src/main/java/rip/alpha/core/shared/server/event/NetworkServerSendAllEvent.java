package rip.alpha.core.shared.server.event;

import rip.alpha.core.shared.bridge.AlphaBridgeEvent;

public record NetworkServerSendAllEvent(String toServer) implements AlphaBridgeEvent {
}
