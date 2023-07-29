package rip.alpha.core.bukkit.essentials;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import rip.alpha.libraries.command.annotation.Command;
import rip.alpha.libraries.command.annotation.CommandDescription;
import rip.alpha.libraries.command.annotation.CommandUsage;
import rip.alpha.libraries.command.annotation.Default;
import rip.alpha.libraries.util.message.MessageBuilder;

public class KillCommand {

    @CommandUsage("(target)")
    @CommandDescription("Kill yourself or another player")
    @Command(names = {"kill"}, permission = "core.command.kill")
    public static void onCommand(CommandSender sender, @Default("self") Player target) {
        if (!sender.equals(target) && !sender.hasPermission("core.command.kill.other")) {
            sender.sendMessage(MessageBuilder.constructError("You dont have the permission to kill others."));
            return;
        }

        target.setHealth(0);
        target.sendMessage(MessageBuilder.construct("You were killed."));

        if (!target.equals(sender)) {
            String message = MessageBuilder.construct("You have killed {}.", target.getName());
            sender.sendMessage(message);
        }
    }

}
