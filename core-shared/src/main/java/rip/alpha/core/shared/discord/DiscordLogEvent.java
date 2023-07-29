package rip.alpha.core.shared.discord;

import rip.alpha.core.shared.bridge.AlphaBridgeEvent;

import java.awt.*;


public record DiscordLogEvent(String serverId, String title, String description,
                              Color color) implements AlphaBridgeEvent {

}
