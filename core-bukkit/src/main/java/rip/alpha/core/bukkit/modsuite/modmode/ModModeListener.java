package rip.alpha.core.bukkit.modsuite.modmode;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import rip.alpha.core.bukkit.modsuite.modmode.menu.OnlineStaffMenu;
import rip.alpha.core.bukkit.modsuite.modmode.menu.PlayerInspectionMenu;
import rip.alpha.core.shared.data.AlphaProfile;
import rip.alpha.core.shared.data.AlphaProfileManager;
import rip.alpha.core.shared.server.NetworkServerHandler;
import rip.alpha.core.shared.server.NetworkServerType;
import rip.alpha.libraries.util.DamagerUtils;
import rip.alpha.libraries.util.message.MessageBuilder;

public class ModModeListener implements Listener {
    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        boolean shouldHideStaff = true;
        if (player.isOp() || player.hasPermission("core.command.modmode")) {
            /*if (AlphaProfileManager.profiles().isLocallyCached(player.getUniqueId())) {
                AlphaProfile profile = AlphaProfileManager.profiles().getCachedValue(player.getUniqueId());
                shouldHideStaff = !profile.getProfileSettings().isShowStaff();
            }*/
            shouldHideStaff = false;
        }

        if (shouldHideStaff) {
            for (Player target : Bukkit.getOnlinePlayers()) {
                ModModeEntry entry = ModModeEntryCache.getInstance().computeIfAbsent(target);
                if (!entry.isVanished()) {
                    continue;
                }
                player.hidePlayer(target);
            }
        }

        if (NetworkServerHandler.getInstance().getCurrentServer().getServerType() != NetworkServerType.PRODUCTION) {
            return;
        }

        if (player.hasPermission("core.command.modmode")) {
            ModModeStatus modSuiteMode = player.hasPermission("core.modmode.admin") ? ModModeStatus.ADMIN : ModModeStatus.MOD;
            ModModeHandler.getInstance().changeStatus(player, modSuiteMode);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        ModModeEntry entry = ModModeEntryCache.getInstance().computeIfAbsent(player);
        if (entry.getModModeStatus() == ModModeStatus.NONE) {
            return;
        }
        ModModeHandler.getInstance().changeStatus(player, ModModeStatus.NONE);
        ModModeEntryCache.getInstance().remove(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        ModModeEntry entry = ModModeEntryCache.getInstance().computeIfAbsent(player);

        if (entry.getModModeStatus() != ModModeStatus.NONE) {
            event.setCancelled(true);
            return;
        }

        Player damager = DamagerUtils.getDamager(event);
        if (damager == null) {
            return;
        }

        ModModeEntry damagerEntry = ModModeEntryCache.getInstance().computeIfAbsent(player);
        if (damagerEntry.getModModeStatus() != ModModeStatus.NONE) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        ModModeEntry entry = ModModeEntryCache.getInstance().computeIfAbsent(player);
        if (entry.getModModeStatus() != ModModeStatus.NONE) {
            if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
                player.teleport(player.getWorld().getSpawnLocation());
            }
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        ModModeEntry entry = ModModeEntryCache.getInstance().computeIfAbsent(player);

        switch (entry.getModModeStatus()) {
            case ADMIN -> {
                if (entry.isVanished()) {
                    event.setCancelled(true);
                }
            }
            case MOD -> event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityTarget(EntityTargetEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        ModModeEntry entry = ModModeEntryCache.getInstance().computeIfAbsent(player);

        if (entry.getModModeStatus() == ModModeStatus.NONE) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ModModeEntry entry = ModModeEntryCache.getInstance().computeIfAbsent(player);

        if (entry.getModModeStatus() == ModModeStatus.NONE) {
            return;
        }

        switch (entry.getModModeStatus()) {
            case ADMIN -> {
                ItemStack itemStack = event.getItemInHand();
                boolean tagged = ModModeHandler.getInstance().isTagged(itemStack);

                if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName() && tagged) {
                    event.setCancelled(true);
                }

                if (entry.isBuild()) {
                    return;
                }

                event.setCancelled(true);
            }
            case MOD -> event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ModModeEntry entry = ModModeEntryCache.getInstance().computeIfAbsent(player);

        if (entry.getModModeStatus() == ModModeStatus.NONE) {
            return;
        }

        if (entry.getModModeStatus() == ModModeStatus.ADMIN && entry.isBuild()) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        ModModeEntry entry = ModModeEntryCache.getInstance().computeIfAbsent(player);

        if (entry.getModModeStatus() == ModModeStatus.NONE) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Item item = event.getItemDrop();
        ModModeEntry entry = ModModeEntryCache.getInstance().computeIfAbsent(player);

        if (entry.getModModeStatus() == ModModeStatus.NONE) {
            return;
        }

        boolean tagged = ModModeHandler.getInstance().isTagged(item.getItemStack());

        if (tagged) {
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);

        if (entry.getModModeStatus() != ModModeStatus.ADMIN) {
            return;
        }

        if (!entry.isVanished()) {
            return;
        }

        item.remove();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onItemSpawn(ItemSpawnEvent event) {
        Item item = event.getEntity();
        ItemStack itemStack = item.getItemStack();

        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return;
        }

        if (!ModModeHandler.getInstance().isTagged(itemStack)) {
            return;
        }

        item.remove();
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        ModModeEntry entry = ModModeEntryCache.getInstance().computeIfAbsent(player);

        if (entry.getModModeStatus() == ModModeStatus.NONE) {
            return;
        }

        ItemStack itemStack = event.getPlayer().getItemInHand();

        if (itemStack == null) {
            return;
        }

        if (itemStack.isSimilar(ModModeHandler.getInstance().getInspectionBookItem())) {
            Entity entity = event.getRightClicked();

            if (!(entity instanceof Player target)) {
                return;
            }

            new PlayerInspectionMenu(target).open(player);
            player.sendMessage(MessageBuilder.construct("You are now inspecting {}.", target.getName()));
        } else if (itemStack.isSimilar(ModModeHandler.getInstance().getFreezeItem())) {
            Entity entity = event.getRightClicked();

            if (!(entity instanceof Player target)) {
                return;
            }

            player.chat("/freeze " + target.getName());
        }  else if (itemStack.isSimilar(ModModeHandler.getInstance().getOnlineStaffItem())) {
            new OnlineStaffMenu().open(player);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ModModeEntry entry = ModModeEntryCache.getInstance().computeIfAbsent(player);
        boolean cancelled = event.isCancelled();

        if (entry.getModModeStatus() == ModModeStatus.NONE) {
            return;
        }

        if (entry.getModModeStatus() == ModModeStatus.MOD || !entry.isBuild()) {
            event.setCancelled(true);
        }

        if (event.hasItem()) {
            ItemStack itemStack = event.getItem();

            if (!ModModeHandler.getInstance().isTagged(itemStack)) {
                return;
            }

            if (itemStack.isSimilar(ModModeHandler.getInstance().getVanishDisableItem())) {
                ModModeHandler.getInstance().showPlayer(player);
                player.getInventory().setItem(4, ModModeHandler.getInstance().getVanishEnableItem());
                player.sendMessage(MessageBuilder.construct("You have unvanished."));
            } else if (itemStack.isSimilar(ModModeHandler.getInstance().getVanishEnableItem())) {
                ModModeHandler.getInstance().vanishPlayer(player);
                player.getInventory().setItem(4, ModModeHandler.getInstance().getVanishDisableItem());
                player.sendMessage(MessageBuilder.construct("You have vanished."));
            } else if (itemStack.isSimilar(ModModeHandler.getInstance().getOnlineStaffItem())) {
                  new OnlineStaffMenu().open(player);
            } else if (itemStack.isSimilar(ModModeHandler.getInstance().getCompassItem())
                    || itemStack.isSimilar(ModModeHandler.getInstance().getWorldEditWandItem())) {
                event.setCancelled(cancelled);
            }
        }
    }
}
