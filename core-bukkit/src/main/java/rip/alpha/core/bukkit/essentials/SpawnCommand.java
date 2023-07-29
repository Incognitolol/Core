package rip.alpha.core.bukkit.essentials;

import org.bukkit.World;
import org.bukkit.entity.Player;
import rip.alpha.libraries.command.annotation.Command;
import rip.alpha.libraries.command.annotation.CommandUsage;
import rip.alpha.libraries.command.annotation.Default;
import rip.alpha.libraries.util.message.MessageBuilder;
import rip.alpha.libraries.util.message.MessageConstants;

public class SpawnCommand {
    @CommandUsage("(world)")
    @Command(names = {"spawn"}, permission = "core.command.spawn")
    public static void spawnCommand(Player player, @Default("self") World world) {
        player.teleport(world.getSpawnLocation());
        player.sendMessage(MessageBuilder.construct("You have been teleported to {} spawn.", MessageConstants.completeWithSOrApostrophe(world.getName())));
    }
}
