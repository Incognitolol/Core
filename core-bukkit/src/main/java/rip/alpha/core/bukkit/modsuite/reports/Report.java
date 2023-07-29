package rip.alpha.core.bukkit.modsuite.reports;

import rip.alpha.core.bukkit.modsuite.ModerationLocation;

import java.util.UUID;

public record Report(
        UUID reporterID,
        String reporterName,
        ModerationLocation reportLocation,
        String serverID,
        String reportedName,
        String reportReason) {

}
