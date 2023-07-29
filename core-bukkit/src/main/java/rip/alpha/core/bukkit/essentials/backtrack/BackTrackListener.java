package rip.alpha.core.bukkit.essentials.backtrack;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import rip.alpha.core.bukkit.data.player.CorePlayerProfileManager;

public class BackTrackListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        CorePlayerProfileManager.profiles()
                .getData(event.getPlayer().getUniqueId())
                .getLocationTrack()
                .addTrack(event.getFrom());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        CorePlayerProfileManager.profiles()
                .getData(event.getPlayer().getUniqueId())
                .getLocationTrack()
                .addTrack(event.getPlayer().getLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        CorePlayerProfileManager.profiles()
                .getData(event.getEntity().getUniqueId())
                .getLocationTrack()
                .addTrack(event.getEntity().getLocation());
    }
}
