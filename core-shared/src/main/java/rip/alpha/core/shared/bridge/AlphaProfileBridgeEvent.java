package rip.alpha.core.shared.bridge;

import java.util.UUID;

public interface AlphaProfileBridgeEvent extends AlphaBridgeEvent {

    @Override
    default void callEvent() {
        AlphaBridgeEvent.super.callEvent();
    }

    UUID profileID();

}