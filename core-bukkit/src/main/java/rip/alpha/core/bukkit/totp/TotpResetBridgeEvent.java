package rip.alpha.core.bukkit.totp;

import rip.alpha.core.shared.bridge.AlphaBridgeEvent;

import java.util.UUID;

public record TotpResetBridgeEvent(UUID uuid) implements AlphaBridgeEvent {

}
