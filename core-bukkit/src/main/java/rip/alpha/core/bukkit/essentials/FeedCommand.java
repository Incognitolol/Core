package rip.alpha.core.bukkit.essentials;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import rip.alpha.libraries.command.annotation.Command;
import rip.alpha.libraries.command.annotation.CommandDescription;
import rip.alpha.libraries.command.annotation.CommandUsage;
import rip.alpha.libraries.command.annotation.Default;
import rip.alpha.libraries.util.message.MessageBuilder;

public class FeedCommand {

    @CommandUsage("(target)")
    @CommandDescription("Feed yourself or another player")
    @Command(names = {"feed"}, permission = "core.command.feed")
    public static void onCommand(CommandSender sender, @Default("self") Player target) {
        if (!sender.equals(target) && !sender.hasPermission("core.command.feed.other")) {
            sender.sendMessage(MessageBuilder.constructError("You dont have the permission to feed others."));
            return;
        }

        target.setFoodLevel(20);
        target.setSaturation(20F);
        target.sendMessage(MessageBuilder.construct("You have been fed."));

        if (!target.equals(sender)) {
            String message = MessageBuilder.construct("You have fed {}.", target.getName());
            sender.sendMessage(message);
        }
    }

}
