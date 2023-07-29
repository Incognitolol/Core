package rip.alpha.core.bukkit.essentials;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import rip.alpha.libraries.command.annotation.Command;
import rip.alpha.libraries.command.annotation.CommandUsage;
import rip.alpha.libraries.command.annotation.Wildcard;
import rip.alpha.libraries.util.message.MessageBuilder;
import rip.alpha.libraries.util.message.MessageTranslator;

public class LocalBroadcastCommand {

    @CommandUsage("[message...]")
    @Command(names = {"localbroadcast", "localbc", "raw"}, async = true, permission = "core.command.localbroadcast")
    public static void localBroadcastCommand(CommandSender sender, @Wildcard String message) {
        sender.sendMessage(MessageBuilder.construct("Locally Broadcasting a message..."));
        Bukkit.broadcastMessage(MessageTranslator.translate(message));
    }

}
