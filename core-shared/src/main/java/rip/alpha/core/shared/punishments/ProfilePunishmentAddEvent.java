package rip.alpha.core.shared.punishments;

import rip.alpha.core.shared.bridge.AlphaProfileBridgeEvent;

import java.util.UUID;

public record ProfilePunishmentAddEvent(UUID profileID, Punishment punishment) implements AlphaProfileBridgeEvent {
}
