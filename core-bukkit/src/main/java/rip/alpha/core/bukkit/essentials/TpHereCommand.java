package rip.alpha.core.bukkit.essentials;

import org.bukkit.entity.Player;
import rip.alpha.libraries.command.annotation.Command;
import rip.alpha.libraries.command.annotation.CommandUsage;
import rip.alpha.libraries.util.message.MessageBuilder;

public class TpHereCommand {

    @CommandUsage("<target>")
    @Command(names = {"tphere", "s"}, permission = "core.command.tphere")
    public static void onCommand(Player player, Player target) {
        target.teleport(player.getLocation());
        player.sendMessage(MessageBuilder.construct("{} has been teleported to your location.", target.getName()));
    }

}
