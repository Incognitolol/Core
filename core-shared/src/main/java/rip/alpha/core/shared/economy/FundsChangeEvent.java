package rip.alpha.core.shared.economy;

import rip.alpha.core.shared.bridge.AlphaBridgeEvent;

import java.math.BigInteger;
import java.util.UUID;

public record FundsChangeEvent(UUID owner, BigInteger before, BigInteger after) implements AlphaBridgeEvent {
}
