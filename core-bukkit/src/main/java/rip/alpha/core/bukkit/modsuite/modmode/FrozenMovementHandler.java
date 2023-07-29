package rip.alpha.core.bukkit.modsuite.modmode;

import net.minecraft.server.v1_7_R4.PacketPlayInFlying;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import rip.foxtrot.spigot.handler.MovementHandler;

/**
 * @author Moose1301
 * @date 4/30/2022
 */
public class FrozenMovementHandler implements MovementHandler {
    @Override
    public void handleUpdateLocation(Player player, Location to, Location from, PacketPlayInFlying packetPlayInFlying) {
        if (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ()) return;

        ModModeEntry targetEntry = ModModeEntryCache.getInstance().get(player.getUniqueId());
        if (targetEntry == null) return;
        if (!targetEntry.isFrozen()) return;

        player.teleport(from);
        player.sendMessage(ChatColor.RED + "You may not move whilst frozen! Please join Teamspeak @ alphamc");
    }
}
