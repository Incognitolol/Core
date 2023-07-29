package rip.alpha.core.bukkit.essentials;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_7_R4.CraftServer;
import org.bukkit.entity.Player;
import rip.alpha.libraries.command.annotation.Command;
import rip.alpha.libraries.command.annotation.CommandUsage;
import rip.alpha.libraries.util.message.MessageBuilder;

public class SetMaxCommand {

    @CommandUsage("<max|1000>")
    @Command(names = {"setmax"}, async = true, permission = "core.command.setmax")
    public static void onCommand(Player player, int max) {
        max = Math.max(0, Math.min(1000, max));
        ((CraftServer) Bukkit.getServer()).getHandle().setMaxPlayers(max);
        player.sendMessage(MessageBuilder.construct("You set the max amount of players to {}.", max));
    }

}
