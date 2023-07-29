package rip.alpha.core.bukkit.essentials;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import rip.alpha.libraries.command.annotation.Command;
import rip.alpha.libraries.command.annotation.CommandUsage;
import rip.alpha.libraries.util.message.MessageBuilder;

public class TpPosCommand {

    @CommandUsage("<x> <y> <z>")
    @Command(names = {"tppos", "teleportposition"}, permission = "core.command.tppos")
    public static void onCommand(Player player, double x, double y, double z) {
        Location targetLocation = player.getLocation().clone();
        targetLocation.setX(x);
        targetLocation.setY(y);
        targetLocation.setZ(z);
        player.teleport(targetLocation);
        String worldName = targetLocation.getWorld().getName();
        player.sendMessage(MessageBuilder.construct("You have been teleported to {}, {}, {} in {}.", x, y, z, worldName));
    }

}
