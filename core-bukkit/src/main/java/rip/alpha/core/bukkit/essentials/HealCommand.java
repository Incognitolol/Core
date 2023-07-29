package rip.alpha.core.bukkit.essentials;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import rip.alpha.libraries.command.annotation.Command;
import rip.alpha.libraries.command.annotation.CommandDescription;
import rip.alpha.libraries.command.annotation.CommandUsage;
import rip.alpha.libraries.command.annotation.Default;
import rip.alpha.libraries.util.message.MessageBuilder;
import rip.alpha.libraries.util.message.MessageConstants;

public class HealCommand {

    @CommandUsage("(target)")
    @CommandDescription("Heal yourself or another player")
    @Command(names = {"heal"}, permission = "core.command.heal")
    public static void onCommand(CommandSender sender, @Default("self") Player target) {
        if (!sender.equals(target) && !sender.hasPermission("core.command.heal.other")) {
            sender.sendMessage(MessageBuilder.constructError("You dont have the permission to heal others."));
            return;
        }

        target.setHealth(target.getMaxHealth());
        target.sendMessage(MessageBuilder.construct("You have been healed."));

        if (!target.equals(sender)) {
            String apName = MessageConstants.completeWithSOrApostrophe(target.getName());
            String message = MessageBuilder.construct("You have set {} health to max.", apName);
            sender.sendMessage(message);
        }
    }

}
