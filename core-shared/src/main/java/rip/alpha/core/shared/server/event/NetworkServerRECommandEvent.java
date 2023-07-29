package rip.alpha.core.shared.server.event;

import rip.alpha.core.shared.bridge.AlphaBridgeEvent;

public record NetworkServerRECommandEvent(String targetServer, String command) implements AlphaBridgeEvent {

}
