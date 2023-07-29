package rip.alpha.core.bukkit.modsuite.chat;

import rip.alpha.core.shared.bridge.AlphaBridgeEvent;
import rip.alpha.libraries.util.message.MessageTranslator;

public record AdminChatEvent(String sender, String server, String message) implements AlphaBridgeEvent {
    public String getFormatted() {
        return MessageTranslator.translate("&c[Admin] &7[%s] &r%s&r&7: &c%s".formatted(server, sender, message));
    }
}
