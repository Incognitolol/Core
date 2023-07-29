package rip.alpha.core.shared.queue.event;

import rip.alpha.core.shared.bridge.AlphaBridgeEvent;

public record NetworkQueueUpdateEvent(String serverId) implements AlphaBridgeEvent {

}
