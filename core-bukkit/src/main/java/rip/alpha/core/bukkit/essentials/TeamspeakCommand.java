package rip.alpha.core.bukkit.essentials;

import org.bukkit.command.CommandSender;
import rip.alpha.libraries.command.annotation.Command;
import rip.alpha.libraries.command.annotation.CommandDescription;
import rip.alpha.libraries.util.message.MessageBuilder;

public class TeamspeakCommand {

    @CommandDescription("Shows the teamspeak server ip")
    @Command(names = {"teamspeak", "ts", "ts3"}, async = true)
    public static void teamspeakCommand(CommandSender sender) {
        sender.sendMessage(MessageBuilder.construct("TeamSpeak: {}", "alphamc"));
    }

}
