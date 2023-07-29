package rip.alpha.core.bukkit.essentials;

import org.bukkit.entity.Player;
import rip.alpha.libraries.command.annotation.Command;
import rip.alpha.libraries.command.annotation.CommandUsage;
import rip.alpha.libraries.util.message.MessageBuilder;

public class TpCommand {

    @CommandUsage("<target>")
    @Command(names = {"tp", "teleport"}, permission = "core.command.tp")
    public static void onCommand(Player player, Player target) {
        player.teleport(target.getLocation());
        player.sendMessage(MessageBuilder.construct("You have been teleported to {}.", target.getName()));
    }

}
