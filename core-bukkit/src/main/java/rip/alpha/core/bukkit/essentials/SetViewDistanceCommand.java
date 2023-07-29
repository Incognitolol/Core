package rip.alpha.core.bukkit.essentials;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_7_R4.CraftWorld;
import rip.alpha.libraries.command.annotation.Command;
import rip.alpha.libraries.command.annotation.CommandUsage;
import rip.alpha.libraries.util.message.MessageBuilder;

public class SetViewDistanceCommand {

    @CommandUsage("(distance|5)")
    @Command(names = {"setviewdistance", "viewdistance"}, permission = "core.command.viewdistance")
    public static void onCommand(CommandSender sender, int distance) {
        if (distance > 10) {
            sender.sendMessage(MessageBuilder.constructError("The maximum view distance is {}.", 10));
            return;
        }

        if (distance < 2) {
            sender.sendMessage(MessageBuilder.constructError("The minimum view distance is {}.", 2));
            return;
        }

        for (World world : Bukkit.getWorlds()) {
            ((CraftWorld) world).getHandle().getPlayerChunkMap().a(distance);
        }

        String message = MessageBuilder.construct("The view distance of all worlds has been set to {}.", distance);
        sender.sendMessage(message);
    }

}
