package rip.alpha.core.bukkit.modsuite.modmode.menu;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import rip.alpha.core.bukkit.modsuite.modmode.ModModeEntry;
import rip.alpha.core.bukkit.modsuite.modmode.ModModeEntryCache;
import rip.alpha.core.bukkit.modsuite.modmode.ModModeStatus;
import rip.alpha.core.shared.data.AlphaProfile;
import rip.alpha.core.shared.data.AlphaProfileManager;
import rip.alpha.core.shared.ranks.Rank;
import rip.alpha.libraries.gui.Button;
import rip.alpha.libraries.gui.Menu;
import rip.alpha.libraries.gui.PaginatedMenu;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;


public class OnlineStaffMenu extends PaginatedMenu {
    public OnlineStaffMenu() {
        super("Online Staff", 18);
    }

    @Override
    protected void setupPaginatedMenu(HumanEntity humanEntity) {
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (!target.hasPermission("core.staff.messages")) {
                continue;
            }

            AlphaProfile profile = AlphaProfileManager.profiles().getCachedValue(target.getUniqueId());
            ModModeEntry entry = ModModeEntryCache.getInstance().computeIfAbsent(target);
            Rank rank = profile.getHighestRank();
            this.addPageElement(this.getButton(target, entry, rank));
        }
    }

    public Button getButton(Player target, ModModeEntry entry, Rank targetRank) {
        return Button.builder()
                .itemCreator(humanEntity -> this.getItemStack(target, entry, targetRank))
                .eventConsumer(event -> this.onClick(event, target))
                .build();
    }

    public ItemStack getItemStack(Player target, ModModeEntry entry, Rank targetRank) {
        ItemStack itemStack = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        ItemMeta itemMeta = itemStack.getItemMeta();
        SkullMeta skullMeta = (SkullMeta) itemMeta;
        skullMeta.setOwner(target.getName());
        skullMeta.setDisplayName(targetRank.getColor() + target.getName());
        Location location = target.getLocation();
        String formattedLocation = location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ();
        List<String> lore = new ArrayList<>();
        lore.add("§7§m----------------------------");
        lore.add("§6ModMode: " + (entry.getModModeStatus() != ModModeStatus.NONE ? (ChatColor.GREEN + "Enabled") : (ChatColor.RED + "Disabled")));
        lore.add("§6Vanish: " + (entry.isVanished() ? (ChatColor.GREEN + "Enabled") : (ChatColor.RED + "Disabled")));
        lore.add("§6Build: " + (entry.isBuild() ? (ChatColor.GREEN + "Enabled") : (ChatColor.RED + "Disabled")));
        lore.add("§6Location: " + formattedLocation);
        lore.add("§6Rank: " + targetRank.getDisplayName());
        lore.add(" ");
        lore.add("§6Click to teleport");
        lore.add("§7§m----------------------------");
        skullMeta.setLore(lore);
        itemStack.setItemMeta(skullMeta);
        return itemStack;
    }

    public void onClick(InventoryClickEvent event, Player target) {
        if (!event.getWhoClicked().hasPermission("core.command.tp")) {
            return;
        }
        event.getWhoClicked().teleport(target);
    }
}
