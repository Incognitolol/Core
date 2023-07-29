package rip.alpha.core.bukkit.economy.shop;

import net.minecraft.server.v1_7_R4.EnchantmentGlow;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import rip.alpha.core.shared.economy.EconomyManager;
import rip.alpha.core.shared.economy.TokenType;
import rip.alpha.libraries.gui.Button;
import rip.alpha.libraries.gui.Menu;
import rip.alpha.libraries.util.NumberUtils;
import rip.alpha.libraries.util.item.ItemBuilder;
import rip.alpha.libraries.util.message.MessageBuilder;
import rip.alpha.libraries.util.message.MessageTranslator;

public class ShopCategoryMenu extends Menu {
    @Override
    protected Inventory createEmptyInventory(HumanEntity humanEntity) {
        return Bukkit.createInventory(null, 9 * 6, ChatColor.GOLD + "Coin Shop");
    }

    @Override
    protected void setup(HumanEntity humanEntity) {
        this.setButton(4, this.getCoinButton());
        this.setButton(ButtonPosition.of(4, 2), this.getRankButton()); //ranks
        super.fillWithPlaceholder(9 * 6);
    }

    private Button getCoinButton() {
        return Button.builder()
                .itemCreator(this::getCoinItemStack)
                .eventConsumer(event -> {
                    event.getWhoClicked().closeInventory();
                    ((Player)event.getWhoClicked()).sendMessage(MessageBuilder.construct("You can purchase more coins at {}",
                            "https://store.alpha.rip"));
                })
                .build();
    }

    private Button getVoteButton() {
        return Button.builder()
                .itemCreator(this::getVoteItemStack)
                .eventConsumer(event -> event.getWhoClicked().closeInventory())
                .build();
    }

    private Button getRankButton() {
        return Button.builder()
                .itemCreator(this::getRankItemStack)
                .eventConsumer(event -> new RankShopMenu().open(event.getWhoClicked()))
                .build();
    }

    private ItemStack getRankItemStack(HumanEntity entity) {
        return new ItemBuilder(Material.DIAMOND_BLOCK)
                .name("&dRanks")
                .lore(MessageTranslator.translate("&f┃ &aVIP"))
                .lore(MessageTranslator.translate("&f┃ &6PRO"))
                .lore(MessageTranslator.translate("&f┃ &9MVP"))
                .lore(MessageTranslator.translate("&f┃ &5HOF"))
                .build();
    }

    private ItemStack getVoteItemStack(HumanEntity entity) {
        return new ItemBuilder(Material.BOOK)
                .name("&eVote")
                .build();
    }

    private ItemStack getCoinItemStack(HumanEntity entity) {
        String balance = NumberUtils.formatBalance(EconomyManager.getFundsSnapshot(entity.getUniqueId(), TokenType.BOUGHT));
        return new ItemBuilder(Material.DOUBLE_PLANT)
                .enchantment(new EnchantmentGlow(-1))
                .name("&6Coins")
                .lore(MessageTranslator.translate("&f┃ &eBalance: &6⛃&f" + balance))
                .lore(MessageTranslator.translate("&f┃ &ePurchase more at &cstore.alpha.rip"))
                .build();
    }
}
