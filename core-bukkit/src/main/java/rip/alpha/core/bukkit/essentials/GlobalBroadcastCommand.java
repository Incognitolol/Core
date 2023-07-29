package rip.alpha.core.bukkit.essentials;

import org.bukkit.command.CommandSender;
import rip.alpha.core.shared.server.event.NetworkServerREBroadcastEvent;
import rip.alpha.libraries.command.annotation.Command;
import rip.alpha.libraries.command.annotation.CommandUsage;
import rip.alpha.libraries.command.annotation.Wildcard;
import rip.alpha.libraries.util.message.MessageBuilder;
import rip.alpha.libraries.util.message.MessageTranslator;

public class GlobalBroadcastCommand {

    @CommandUsage("[message...]")
    @Command(names = {"broadcast", "bc", "globalbroadcast", "globalbc"}, async = true, permission = "core.command.broadcast")
    public static void broadcastCommand(CommandSender sender, @Wildcard String message) {
        sender.sendMessage(MessageBuilder.construct("Broadcasting message..."));
        new NetworkServerREBroadcastEvent("bukkit", MessageTranslator.translate(message), "").callEvent();
    }
}
