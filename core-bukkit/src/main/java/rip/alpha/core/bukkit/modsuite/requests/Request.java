package rip.alpha.core.bukkit.modsuite.requests;

import rip.alpha.core.bukkit.modsuite.ModerationLocation;

import java.util.UUID;

public record Request(
        UUID requesterID,
        String requesterName,
        ModerationLocation requestLocation,
        String serverID,
        String requestMessage) {

}
