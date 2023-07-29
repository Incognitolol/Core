package rip.alpha.core.bukkit.essentials;

import org.bukkit.command.CommandSender;
import rip.alpha.libraries.command.annotation.Command;
import rip.alpha.libraries.util.message.MessageBuilder;

public class DiscordCommand {

    @Command(names = {"discord", "disc"}, async = true)
    public static void discordCommand(CommandSender sender) {
        String message = MessageBuilder.construct("Discord: {}", "https://discord.gg/nM7aXWsjsF");
        sender.sendMessage(message);
    }

}
