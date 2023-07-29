package rip.alpha.core.bukkit.modsuite.reports;

import rip.alpha.core.shared.bridge.AlphaBridgeEvent;

public record PlayerReportEvent(Report report) implements AlphaBridgeEvent {
}
