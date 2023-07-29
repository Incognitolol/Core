package rip.alpha.core.bukkit.modsuite.modmode;

import com.lunarclient.bukkitapi.LunarClientAPI;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import rip.alpha.core.bukkit.Core;
import rip.alpha.core.bukkit.modsuite.modmode.event.PlayerEnterModModeEvent;
import rip.alpha.core.bukkit.modsuite.modmode.event.PlayerExitModModeEvent;
import rip.alpha.core.shared.data.AlphaProfile;
import rip.alpha.core.shared.data.AlphaProfileManager;
import rip.alpha.libraries.util.item.ItemBuilder;
import rip.alpha.libraries.util.item.LoreUtil;
import rip.alpha.libraries.util.message.MessageBuilder;
import rip.alpha.libraries.util.message.MessageColor;
import rip.foxtrot.spigot.fSpigot;

public class ModModeHandler {

    @Getter
    private static final ModModeHandler instance = new ModModeHandler();

    private final ItemStack[] inventoryContents = new ItemStack[9 * 4];

    @Getter(AccessLevel.PROTECTED)
    private final ItemStack worldEditWandItem =
            this.tagItem(new ItemBuilder(Material.WOOD_AXE)
                    .build());

    @Getter(AccessLevel.PROTECTED)
    private final ItemStack vanishDisableItem =
            this.tagItem(new ItemBuilder(Material.INK_SACK)
                    .durability(10)
                    .name("&6Become Invisible")
                    .build());

    @Getter(AccessLevel.PROTECTED)
    private final ItemStack vanishEnableItem =
            this.tagItem(new ItemBuilder(Material.INK_SACK)
                    .durability(8)
                    .name("&6Become Visible")
                    .build());

    @Getter(AccessLevel.PROTECTED)
    private final ItemStack compassItem = tagItem(new ItemBuilder(Material.COMPASS)
            .name("&6Teleport Tool")
            .build());

    @Getter(AccessLevel.PROTECTED)
    private final ItemStack freezeItem = tagItem(new ItemBuilder(Material.PACKED_ICE)
            .name("&6Freeze")
            .build());

    @Getter(AccessLevel.PROTECTED)
    private final ItemStack inspectionBookItem = tagItem(new ItemBuilder(Material.BOOKSHELF)
            .name("&6Inventory Inspection")
            .build());

    @Getter(AccessLevel.PROTECTED)
    private final ItemStack lastPvP = tagItem(new ItemBuilder(Material.EMERALD)
            .name("&6Teleport to Last PVP")
            .build());

    @Getter(AccessLevel.PROTECTED)
    private final ItemStack onlineStaffItem = tagItem(new ItemBuilder(Material.SKULL_ITEM)
            .durability(3)
            .name("&6Online Staff")
            .build());

    private ModModeHandler() {
        this.inventoryContents[0] = compassItem;
        this.inventoryContents[1] = freezeItem;
        this.inventoryContents[3] = inspectionBookItem;
        this.inventoryContents[4] = vanishDisableItem;
        this.inventoryContents[5] = lastPvP;
        this.inventoryContents[8] = onlineStaffItem;
        fSpigot.INSTANCE.addMovementHandler(new FrozenMovementHandler());
        Bukkit.getServer().getPluginManager().registerEvents(new FrozenListener(), Core.getInstance());
    }

    protected void changeStatus(Player player, ModModeStatus mode) {
        ModModeEntry entry = ModModeEntryCache.getInstance().computeIfAbsent(player);
        entry.setModModeStatus(mode);

        switch (mode) {
            case ADMIN -> {
                this.resetPlayer(player);
                LunarClientAPI.getInstance().giveAllStaffModules(player);

                this.vanishPlayer(player);
                player.setAllowFlight(true);
                player.setFlying(true);
                player.setGameMode(GameMode.CREATIVE);
                player.getInventory().setContents(this.inventoryContents);

                if (player.hasPermission("worldedit.wand")) {
                    player.getInventory().setItem(7, this.worldEditWandItem);
                }

                player.updateInventory();
                player.sendMessage(MessageBuilder.construct("You have enabled admin mode"));
                new PlayerEnterModModeEvent(player).callEvent();
            }
            case MOD -> {
                this.resetPlayer(player);
                LunarClientAPI.getInstance().giveAllStaffModules(player);
                this.vanishPlayer(player);
                player.setAllowFlight(true);
                player.setFlying(true);
                player.setGameMode(GameMode.SURVIVAL);
                player.getInventory().setContents(this.inventoryContents);
                player.updateInventory();
                player.sendMessage(MessageBuilder.construct("You have enabled mod mode"));
                new PlayerEnterModModeEvent(player).callEvent();
            }
            case NONE -> {
                this.resetPlayer(player);
                this.showPlayer(player);
                player.sendMessage(MessageBuilder.construct("You have disabled mod mode"));
                new PlayerExitModModeEvent(player).callEvent();
            }
        }
    }

    protected void showPlayer(Player player) {
        ModModeEntry entry = ModModeEntryCache.getInstance().computeIfAbsent(player);
        entry.setVanished(false);
        for (Player target : Bukkit.getOnlinePlayers()) {
            target.showPlayer(player);
        }
    }

    protected void vanishPlayer(Player player) {
        ModModeEntry entry = ModModeEntryCache.getInstance().computeIfAbsent(player);
        entry.setVanished(true);

        for (Player target : Bukkit.getOnlinePlayers()) {
            if (!target.isOp() && !target.hasPermission("core.command.modmode")) {
                target.hidePlayer(player);
                continue;
            }

            if (AlphaProfileManager.profiles().isLocallyCached(player.getUniqueId())) {
                AlphaProfile profile = AlphaProfileManager.profiles().getCachedValue(player.getUniqueId());
                if (profile.getProfileSettings().isShowStaff()) {
                    continue;
                }
            }

            target.hidePlayer(player);
        }
    }

    private void resetPlayer(Player player) {
        LunarClientAPI.getInstance().disableAllStaffModules(player);

        ModModeEntry entry = ModModeEntryCache.getInstance().computeIfAbsent(player);
        entry.setBuild(false);
        entry.setVanished(false);

        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setSaturation(20);
        player.setLevel(0);
        player.setExp(0);

        if (!player.isOp()) {
            player.setGameMode(GameMode.SURVIVAL);
            player.setFlying(false);
            player.setAllowFlight(false);
        }
    }

    protected boolean isTagged(ItemStack itemStack) {
        String lore = LoreUtil.getFirstLoreLine(itemStack);
        if (lore == null) {
            return false;
        }
        return lore.equalsIgnoreCase(MessageColor.GOLD + "Staff Mode Item");
    }

    private ItemStack tagItem(ItemStack itemStack) {
        return new ItemBuilder(itemStack)
                .lore(MessageColor.GOLD + "Staff Mode Item")
                .build();
    }
}
