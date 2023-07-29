package rip.alpha.core.bukkit.essentials;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import rip.alpha.libraries.command.annotation.Command;
import rip.alpha.libraries.command.annotation.CommandUsage;
import rip.alpha.libraries.command.annotation.Default;
import rip.alpha.libraries.util.message.MessageBuilder;

public class FlyCommand {

    @CommandUsage("(target)")
    @Command(names = {"fly"}, permission = "core.command.fly")
    public static void onCommand(CommandSender sender, @Default("self") Player target) {
        if (!sender.equals(target) && !sender.hasPermission("core.command.fly.other")) {
            sender.sendMessage(MessageBuilder.constructError("You dont have the permission to toggle fly for others."));
            return;
        }

        target.setAllowFlight(!target.getAllowFlight());
        target.setFlying(target.getAllowFlight());

        if (target.getAllowFlight()) {
            target.sendMessage(MessageBuilder.construct("You are now able to fly."));
        } else {
            target.sendMessage(MessageBuilder.construct("You are no longer able to fly."));
        }

        if (!target.equals(sender)) {
            String message = MessageBuilder.standard("You have {} flight for {}.")
                    .element(target.getAllowFlight() ? "enabled" : "disabled")
                    .element(target.getName())
                    .build();
            sender.sendMessage(message);
        }
    }

}
