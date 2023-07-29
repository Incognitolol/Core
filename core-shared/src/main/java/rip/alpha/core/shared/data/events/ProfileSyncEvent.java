package rip.alpha.core.shared.data.events;

import rip.alpha.core.shared.bridge.AlphaBridgeEvent;

import java.util.UUID;


public record ProfileSyncEvent(String code, UUID profileID) implements AlphaBridgeEvent {

}
