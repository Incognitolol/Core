package rip.alpha.core.bukkit.economy.shop;

import lombok.RequiredArgsConstructor;
import net.minecraft.server.v1_7_R4.EnchantmentGlow;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import rip.alpha.core.shared.data.AlphaProfile;
import rip.alpha.core.shared.data.AlphaProfileManager;
import rip.alpha.core.shared.economy.EconomyManager;
import rip.alpha.core.shared.economy.EconomyResponse;
import rip.alpha.core.shared.economy.TokenType;
import rip.alpha.core.shared.grants.Grant;
import rip.alpha.core.shared.grants.ProfileGrantRemoveEvent;
import rip.alpha.core.shared.ranks.Rank;
import rip.alpha.libraries.gui.Button;
import rip.alpha.libraries.gui.ConfirmationMenu;
import rip.alpha.libraries.gui.Menu;
import rip.alpha.libraries.util.item.ItemBuilder;
import rip.alpha.libraries.util.message.MessageBuilder;
import rip.alpha.libraries.util.message.MessageConstants;

import java.time.Duration;
import java.util.Collection;
import java.util.List;

public class RankShopMenu extends Menu {
    @Override
    protected Inventory createEmptyInventory(HumanEntity humanEntity) {
        return Bukkit.createInventory(null, 9 * 5, ChatColor.GOLD + "Select a rank...");
    }

    @Override
    protected void setup(HumanEntity humanEntity) {
        //VIP
        List<String> vipPerks = List.of(
                "1 life reclaim",
                "Deathban reduced by 30 minutes",
                "Higher queue priority than &fDefault"
        );
        this.setButton(10, this.getRankButton(Rank.VIP, vipPerks, 15000, 35000, 75000, 170000));
        this.setButton(10 + (9 * 2), this.getRankUpgradeButton(Rank.VIP_PLUS, Rank.VIP));

        //PRO
        List<String> proPerks = List.of(
                "2 lives reclaim",
                "Deathban reduced by 1 hour",
                "Higher queue priority than &aVIP+"
        );
        this.setButton(12, this.getRankButton(Rank.PRO, proPerks, 30000, 65000, 95000, 230000));
        this.setButton(12 + (9 * 2), this.getRankUpgradeButton(Rank.PRO_PLUS, Rank.PRO));

        //MVP
        List<String> mvpPerks = List.of(
                "3 lives reclaim",
                "Deathban reduced by 1 hour and 15 minutes",
                "Higher queue priority than &6PRO+"
        );
        this.setButton(14, this.getRankButton(Rank.MVP, mvpPerks, 45000, 80000, 115000, 300000));
        this.setButton(14 + (9 * 2), this.getRankUpgradeButton(Rank.MVP_PLUS, Rank.MVP));

        //HOF
        List<String> hofPerks = List.of(
                "4 lives reclaim",
                "Deathban reduced by 1 hour and 30 minutes",
                "Higher queue priority than &9MVP+"
        );
        this.setButton(25, this.getRankButton(Rank.HOF, hofPerks, 65000, 110000, 300000, 600000));

        //Fill Menu
        super.fillWithPlaceholder(9 * 5);
    }

    private Button getRankButton(Rank rank, Collection<String> perks, int... prices) {
        if (prices.length < 4) {
            throw new IllegalArgumentException("Gave too few args");
        }

        return Button.builder()
                .itemCreator(entity -> this.getRankItemStack((Player) entity, rank, perks, prices))
                .eventConsumer(event -> {
                    if (!event.isLeftClick()) {
                        return;
                    }
                    new SelectRankDurationMenu(rank, prices).open(event.getWhoClicked());
                })
                .build();
    }

    private Button getRankUpgradeButton(Rank rank, Rank requiredRank) {
        return Button.builder()
                .itemCreator(entity -> this.getRankUpgradeItemStack((Player) entity, rank, requiredRank))
                .eventConsumer(event -> {
                    if (!event.isLeftClick()) {
                        return;
                    }

                    new ConfirmationMenu("Are you sure?", aBoolean -> {
                        if (aBoolean) {
                            this.handleUpgradeRank((Player) event.getWhoClicked(), rank, requiredRank);
                        }
                        event.getWhoClicked().closeInventory();
                    }).open(event.getWhoClicked());
                })
                .build();
    }

    private ItemStack getRankItemStack(Player player, Rank rank, Collection<String> perks, int[] prices) {
        ItemBuilder builder = new ItemBuilder(Material.LEATHER_CHESTPLATE)
                .color(this.getRGBColor(rank))
                .name(rank.getDisplayName())
                .lore("");

        if (perks.size() > 0) {
            builder.lore("&6Perks:");
            perks.forEach(perk -> builder.lore(" &f┃ &e" + perk));
            builder.lore("");
        }

        if (rank == Rank.HOF) {
            builder.enchantment(new EnchantmentGlow(-1));
        }

        AlphaProfile alphaProfile = AlphaProfileManager.profiles().getCachedValue(player.getUniqueId());
        if (alphaProfile.getHighestRank().isHigherOrEqualTo(rank)) {
            builder.name("&7&m" + rank.getName());
        }

        return builder.lore("&6Left click to view prices.").build();
    }

    private ItemStack getRankUpgradeItemStack(Player player, Rank rank, Rank requiredRank) {
        ItemBuilder builder = new ItemBuilder(Material.LEATHER_CHESTPLATE)
                .color(this.getRGBColor(rank))
                .lore("")
                .lore("&6Perks:")
                .lore(" &f┃ " + MessageConstants.completeWithSOrApostrophe(requiredRank.getDisplayName()) + " &eperks&f.")
                .lore(" &f┃ " + "&eImproved queue priority than " + requiredRank.getDisplayName() + "&f.")
                .lore("")
                .lore("&cDisclaimer")
                .lore(" &c┃ This will replace your current " + requiredRank.getDisplayName() + " &cwith")
                .lore(" &c┃ " + rank.getDisplayName() + "&c. This also goes for the remaining")
                .lore(" &c┃ duration of your current " + requiredRank.getDisplayName() + " &crank.")
                .lore("")
                .name(rank.getDisplayName() + " &7(Upgrade)");

        AlphaProfile alphaProfile = AlphaProfileManager.profiles().getCachedValue(player.getUniqueId());

        if (alphaProfile.getHighestRank().isHigherOrEqualTo(rank)) {
            builder.name("&7&m" + rank.getName() + " (Upgrade)");
            builder.lore("&cYou already have a rank that is");
            builder.lore("&chigher or equal to this upgrade.");
        } else if (!alphaProfile.getHighestRank().equals(requiredRank)) {
            builder.lore("&cYou need " + requiredRank.getDisplayName() + " &cto apply this upgrade.");
        } else {
            builder.lore("&6Left click to upgrade your " + requiredRank.getDisplayName() + " &6for ⛃&e10000&e.");
            builder.enchantment(new EnchantmentGlow(-1));
        }

        return builder.build();
    }

    private Color getRGBColor(Rank rank) {
        java.awt.Color javaColor = rank.getJavaColor();
        int red = javaColor.getRed(), green = javaColor.getGreen(), blue = javaColor.getBlue();
        return Color.fromRGB(red, green, blue);
    }

    private void handleUpgradeRank(Player player, Rank rank, Rank requiredRank) {
        AlphaProfileManager.profiles().applyToDataAsync(player.getUniqueId(), alphaProfile -> {
            if (alphaProfile.getHighestRank().isHigherOrEqualTo(rank)) {
                player.sendMessage(MessageBuilder.constructError("You already have a rank that is equal or higher than this upgrade."));
                return;
            }

            if (!alphaProfile.getHighestRank().equals(requiredRank)) {
                player.sendMessage(MessageBuilder.constructError("You need to have {} to upgrade into {}.", requiredRank.getDisplayName(), rank.getDisplayName()));
                return;
            }

            EconomyResponse response = EconomyManager.removeFromFunds("CONSOLE", player.getUniqueId(), TokenType.BOUGHT, 10000);

            if (response == EconomyResponse.LOW_FUNDS) {
                player.sendMessage(MessageBuilder.constructError("You do not have enough coins to purchase this upgrade."));
                return;
            }

            if (response != EconomyResponse.SUCCESS) {
                player.sendMessage(MessageBuilder.constructError("Something went wrong while upgrading your rank."));
                return;
            }

            List<Grant> grants = alphaProfile.getGrantsForRank(requiredRank);
            grants.forEach(grant -> {
                alphaProfile.removeGrant(grant, ProfileGrantRemoveEvent.Reason.REPLACED, "CONSOLE", "Upgraded");
                long duration = grant.isInfinite() ? -1 : grant.getTimeLeft();
                Grant upgradedGrant = new Grant(rank, "CONSOLE", "Upgraded", duration);
                alphaProfile.addGrant(upgradedGrant);
            });

            player.sendMessage(MessageBuilder.construct("You have successfully upgraded from {} to {}.", requiredRank.getDisplayName(), rank.getDisplayName()));
        });
    }

    @RequiredArgsConstructor
    public static class SelectRankDurationMenu extends Menu {

        private final Rank rank;
        private final int[] prices;

        @Override
        protected Inventory createEmptyInventory(HumanEntity humanEntity) {
            return Bukkit.createInventory(null, 9 * 3, "Select a duration...");
        }

        @Override
        protected void setup(HumanEntity humanEntity) {
            this.setButton(10, this.get30DayButton());
            this.setButton(12, this.get90DayButton());
            this.setButton(14, this.get1YearButton());
            this.setButton(16, this.getPermanentButton());
            super.fillWithPlaceholder(9 * 3);
        }

        private Button get30DayButton() {
            return Button.builder()
                    .itemCreator(entity -> this.getItemStack(this.prices[0], "30 days"))
                    .eventConsumer(event -> this.askForConfirmation(event.getWhoClicked(), this.prices[0], Duration.ofDays(30).toMillis()))
                    .build();
        }

        private Button get90DayButton() {
            return Button.builder()
                    .itemCreator(entity -> this.getItemStack(this.prices[1], "90 days"))
                    .eventConsumer(event -> this.askForConfirmation(event.getWhoClicked(), this.prices[1], Duration.ofDays(90).toMillis()))
                    .build();
        }

        private Button get1YearButton() {
            return Button.builder()
                    .itemCreator(entity -> this.getItemStack(this.prices[2], "1 year"))
                    .eventConsumer(event -> this.askForConfirmation(event.getWhoClicked(), this.prices[2], Duration.ofDays(365).toMillis()))
                    .build();
        }

        private Button getPermanentButton() {
            return Button.builder()
                    .itemCreator(entity -> this.getItemStack(this.prices[3], "Permanent"))
                    .eventConsumer(event -> this.askForConfirmation(event.getWhoClicked(), this.prices[3], -1))
                    .build();
        }

        private ItemStack getItemStack(int price, String name) {
            boolean perm = name.equalsIgnoreCase("permanent");
            return new ItemBuilder(Material.PAPER)
                    .name("&6" + name)
                    .lore("&6Left click to " + (perm ? "temporarily" : "permanently") + " purchase " + this.rank.getDisplayName() + " &6for ⛃&e" + price)
                    .build();
        }

        private void askForConfirmation(HumanEntity entity, int price, long duration) {
            new ConfirmationMenu("Are you sure?", aBoolean -> {
                if (aBoolean) {
                    this.handleConfirmation((Player) entity, price, duration);
                }
                entity.closeInventory();
            }).open(entity);
        }

        private void handleConfirmation(Player player, int price, long duration) {
            AlphaProfileManager.profiles().applyToDataAsync(player.getUniqueId(), alphaProfile -> {
                if (alphaProfile.getHighestRank().isHigherOrEqualTo(rank)) {
                    player.sendMessage(MessageBuilder.constructError("You already have a rank that is equal or higher than this upgrade."));
                    return;
                }

                EconomyResponse response = EconomyManager.removeFromFunds("CONSOLE", player.getUniqueId(), TokenType.BOUGHT, price);

                if (response == EconomyResponse.LOW_FUNDS) {
                    player.sendMessage(MessageBuilder.constructError("You do not have enough coins to purchase this rank."));
                    return;
                }

                if (response != EconomyResponse.SUCCESS) {
                    player.sendMessage(MessageBuilder.constructError("Something went wrong while purchasing this rank."));
                    return;
                }

                Grant grant = new Grant(rank, "CONSOLE", "Purchased", duration);
                alphaProfile.addGrant(grant);
                player.sendMessage(MessageBuilder.construct("You have successfully purchased {}.", rank.getDisplayName()));
            });
        }
    }
}
