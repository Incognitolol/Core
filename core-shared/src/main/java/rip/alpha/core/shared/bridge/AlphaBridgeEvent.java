package rip.alpha.core.shared.bridge;

import rip.alpha.bridge.BridgeEvent;
import rip.alpha.core.shared.AlphaCore;

public interface AlphaBridgeEvent extends BridgeEvent {

    default void callEvent() {
        AlphaCore.callEvent(this);
    }

}
