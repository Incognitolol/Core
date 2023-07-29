package rip.alpha.core.shared.server.event;

import rip.alpha.core.shared.bridge.AlphaBridgeEvent;

public record NetworkServerREShutdownEvent(String targetServer, int duration) implements AlphaBridgeEvent {

}
