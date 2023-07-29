package rip.alpha.core.shared.punishments;

import lombok.AllArgsConstructor;
import lombok.Getter;
import rip.alpha.core.shared.bridge.AlphaProfileBridgeEvent;

import java.util.UUID;

public record ProfilePunishmentRemoveEvent(UUID profileID, Punishment punishment,
                                           Reason reason,
                                           String removedBy,
                                           String removeReason) implements AlphaProfileBridgeEvent {


    @Getter
    @AllArgsConstructor
    public enum Reason {
        ADDED_ANOTHER,
        RUN_OUT,
        LIFTED
    }
}
