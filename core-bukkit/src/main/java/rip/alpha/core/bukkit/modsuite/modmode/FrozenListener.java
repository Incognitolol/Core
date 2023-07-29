package rip.alpha.core.bukkit.modsuite.modmode;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 * @author Moose1301
 * @date 4/30/2022
 */
public class FrozenListener implements Listener {
    @EventHandler
    public void onPlayerInteract(final PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ModModeEntry profile = ModModeEntryCache.getInstance().computeIfAbsent(player);
        if (profile == null) {
            return;
        }
        if (profile.isFrozen()) {
            event.setCancelled(true);
            player.updateInventory();
        }
    }
    @EventHandler
    public void onPlayerDropItem(final PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        ModModeEntry profile = ModModeEntryCache.getInstance().computeIfAbsent(player);
        if (profile == null) {
            return;
        }
        if (profile.isFrozen()) {
            event.setCancelled(true);
            player.updateInventory();
        }
    }
    @EventHandler
    public void onInventoryClick(final InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ModModeEntry profile = ModModeEntryCache.getInstance().computeIfAbsent(player);
        if (profile == null) {
            return;
        }
        if (profile.isFrozen()) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            return;
        }
        Player player = event.getPlayer();
        ModModeEntry profile = ModModeEntryCache.getInstance().computeIfAbsent(player);
        if (profile == null) {
            return;
        }
        if (profile.isFrozen()) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {

            return;
        }

        ModModeEntry profile = ModModeEntryCache.getInstance().computeIfAbsent(player);
        if (profile == null) {
            return;
        }
        if (profile.isFrozen()) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {

            return;
        }

        ModModeEntry profile = ModModeEntryCache.getInstance().computeIfAbsent(player);
        if (profile == null) {
            return;
        }
        if (profile.isFrozen()) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ModModeEntry profile = ModModeEntryCache.getInstance().computeIfAbsent(player);
        if (profile == null) {
            return;
        }
        if (profile.isFrozen()) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ModModeEntry profile = ModModeEntryCache.getInstance().computeIfAbsent(player);
        if (profile == null) {
            return;
        }
        if (profile.isFrozen()) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onPreProcessCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        ModModeEntry profile = ModModeEntryCache.getInstance().computeIfAbsent(player);
        if (profile == null) {
            return;
        }
        if (profile.isFrozen()) {
            event.setCancelled(true);
            if (event.getMessage().startsWith("/msg") || event.getMessage().startsWith("/r")) {
                return;
            }
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You may not use commands while frozen.");
        }
    }
}
