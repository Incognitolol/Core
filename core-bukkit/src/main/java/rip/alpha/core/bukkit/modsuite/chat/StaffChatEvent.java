package rip.alpha.core.bukkit.modsuite.chat;

import rip.alpha.core.shared.bridge.AlphaBridgeEvent;
import rip.alpha.libraries.util.message.MessageTranslator;

public record StaffChatEvent(String sender, String server, String message) implements AlphaBridgeEvent {
    public String getFormatted() {
        return MessageTranslator.translate("&9[Staff] &7[%s] &r%s&r&7: &b%s".formatted(server, sender, message));
    }
}
