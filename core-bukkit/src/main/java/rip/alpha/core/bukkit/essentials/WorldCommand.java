package rip.alpha.core.bukkit.essentials;

import org.bukkit.World;
import org.bukkit.entity.Player;
import rip.alpha.libraries.command.annotation.Command;
import rip.alpha.libraries.command.annotation.CommandUsage;
import rip.alpha.libraries.command.annotation.Default;
import rip.alpha.libraries.util.message.MessageBuilder;
import rip.alpha.libraries.util.message.MessageColor;
import rip.alpha.libraries.util.message.MessageConstants;

public class WorldCommand {
    @CommandUsage("<world> (target)")
    @Command(names = {"world"}, permission = "core.command.world")
    public static void worldCommand(Player player, World world, @Default("self") Player target) {
        if (!player.equals(target) && !player.hasPermission("core.command.world.other")){
            player.sendMessage(MessageBuilder.constructError("You do not have permissions to change the world for others."));
            return;
        }

        String worldName = MessageConstants.completeWithSOrApostrophe(world.getName());
        player.teleport(world.getSpawnLocation());
        player.sendMessage(MessageBuilder.construct("You have been teleported to {} spawn.", worldName));

        if (!player.equals(target)){
            player.sendMessage(MessageBuilder.construct("You have teleported {} to {} spawns.", target.getName(), worldName));
        }
    }
}
