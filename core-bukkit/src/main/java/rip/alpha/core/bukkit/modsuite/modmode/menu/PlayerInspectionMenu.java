package rip.alpha.core.bukkit.modsuite.modmode.menu;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import rip.alpha.libraries.gui.Button;
import rip.alpha.libraries.gui.Menu;
import rip.alpha.libraries.util.TimeUtil;
import rip.alpha.libraries.util.item.ItemBuilder;
import rip.alpha.libraries.util.message.MessageColor;
import rip.alpha.libraries.util.message.MessageTranslator;

@RequiredArgsConstructor
public class PlayerInspectionMenu extends Menu {

    private final Player target;

    @Override
    protected Inventory createEmptyInventory(HumanEntity humanEntity) {
        return Bukkit.createInventory(humanEntity, 9 * 6, MessageColor.GOLD + "Inspecting...");
    }

    @Override
    protected void setup(HumanEntity humanEntity) {
        ItemStack[] contents = target.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            if (target.getInventory().getContents()[i] != null) {
                this.setButton(i, this.getSimpleButton(contents[i]));
            }
        }

        for (int i = 36; i < 44; i++) {
            this.setButton(i, this.getSimpleButton(new ItemBuilder(Material.STAINED_GLASS_PANE).name("").durability((short) 7).build()));
        }

        this.setButton(45, this.getSimpleButton(this.target.getInventory().getHelmet()));
        this.setButton(46, this.getSimpleButton(this.target.getInventory().getChestplate()));
        this.setButton(47, this.getSimpleButton(this.target.getInventory().getLeggings()));
        this.setButton(48, this.getSimpleButton(this.target.getInventory().getBoots()));
        this.setButton(49, this.getSimpleButton(this.target.getInventory().getItemInHand()));
        if(humanEntity.hasPermission("core.invsee.edit")) {
            this.setButton(53, this.getEditInventoryButton());
        }

        this.setButton(50, this.getPotionsButton());
        this.setButton(51, this.getHealthButton());
        this.setButton(52, this.getFoodButton());
    }

    private Button getHealthButton() {
        ItemStack itemStack = new ItemBuilder(Material.SPECKLED_MELON)
                .amount((int) Math.floor(target.getHealth()))
                .name("&fHealth: &a" + target.getHealth())
                .build();
        return this.getSimpleButton(itemStack);
    }

    private Button getFoodButton() {
        ItemStack itemStack = new ItemBuilder(Material.GOLDEN_CARROT)
                .amount((int) Math.floor(target.getFoodLevel()))
                .name("&fFood: &a" + target.getFoodLevel())
                .build();
        return this.getSimpleButton(itemStack);
    }

    private Button getPotionsButton() {
        ItemBuilder potions = new ItemBuilder(Material.POTION);
        potions.name("&bPotion Effects");
        for (PotionEffect effect : target.getActivePotionEffects()) {
            String name = StringUtils.capitalize(effect.getType().getName().toLowerCase().replace("_", " "));
            potions.lore(MessageTranslator.translate("&f" + name + "&f: &4" + TimeUtil.formatIntoMMSS(effect.getDuration())));
        }

        if (target.getActivePotionEffects().isEmpty()) {
            potions.lore(MessageTranslator.translate("&bNone"));
        }

        return this.getSimpleButton(potions.build());
    }

    private Button getEditInventoryButton() {
        return Button
                .builder()
                .itemCreator(humanEntity -> new ItemBuilder(Material.BOOK).name("&6Modify").build())
                .eventConsumer(event -> event.getWhoClicked().openInventory(this.target.getInventory()))
                .build();
    }

    private Button getSimpleButton(ItemStack itemStack) {
        return Button
                .builder()
                .itemCreator(humanEntity -> itemStack == null ? new ItemStack(Material.AIR) : itemStack)
                .eventConsumer(event -> event.setCancelled(true))
                .build();
    }
}
