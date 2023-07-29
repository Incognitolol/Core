package rip.alpha.core.shared.server.event;

import rip.alpha.core.shared.bridge.AlphaBridgeEvent;

public record NetworkServerREBroadcastEvent(String targetServer, String translatedMessage,
                                            String permission) implements AlphaBridgeEvent {
}
