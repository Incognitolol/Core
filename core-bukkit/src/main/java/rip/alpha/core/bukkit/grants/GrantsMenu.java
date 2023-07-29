package rip.alpha.core.bukkit.grants;

import net.minecraft.server.v1_7_R4.EnchantmentGlow;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import rip.alpha.core.shared.data.AlphaProfile;
import rip.alpha.core.shared.data.AlphaProfileManager;
import rip.alpha.core.shared.data.GrantHistory;
import rip.alpha.core.shared.grants.Grant;
import rip.alpha.core.shared.grants.ProfileGrantRemoveEvent;
import rip.alpha.core.shared.ranks.Rank;
import rip.alpha.libraries.chatinput.ChatInput;
import rip.alpha.libraries.gui.Button;
import rip.alpha.libraries.gui.PaginatedMenu;
import rip.alpha.libraries.util.DateUtil;
import rip.alpha.libraries.util.TimeUtil;
import rip.alpha.libraries.util.item.ItemBuilder;
import rip.alpha.libraries.util.message.MessageBuilder;
import rip.alpha.libraries.util.task.TaskUtil;

import java.awt.*;
import java.text.DateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.List;

public class GrantsMenu extends PaginatedMenu {

    private final List<Grant> activeGrants;
    private final GrantHistory grantHistory;
    private final UUID targetID;

    public static void openFor(Player player, UUID targetID) {
        player.sendMessage(MessageBuilder.construct("Loading..."));
        AlphaProfileManager.profiles().getOrCreateRealTimeDataAsync(targetID).thenAccept(profile ->
                TaskUtil.runSync(() -> openFor(player, profile)));
    }

    public static void openFor(Player player, AlphaProfile profile) {
        new GrantsMenu(profile.getAllGrants(), profile.getGrantHistory(), profile.getMojangID()).open(player);
    }

    private GrantsMenu(List<Grant> activeGrants, GrantHistory grantHistory, UUID targetID) {
        super("&6Grants", 4 * 9);
        this.activeGrants = activeGrants;
        this.grantHistory = grantHistory;
        this.targetID = targetID;
    }

    @Override
    protected void setupPaginatedMenu(HumanEntity player) {
        ListIterator<Grant> activePunishmentIterator = this.activeGrants.listIterator(this.activeGrants.size());
        while (activePunishmentIterator.hasPrevious()) {
            this.addPageElement(this.getGrantIcon(activePunishmentIterator.previous()));
        }

        ListIterator<GrantHistory.Entry> grantHistoryIterator = this.grantHistory.getEntries().listIterator(this.grantHistory.getEntries().size());
        while (grantHistoryIterator.hasPrevious()) {
            this.addPageElement(this.getHistoryIcon(grantHistoryIterator.previous()));
        }
    }

    private Button getGrantIcon(Grant grant) {
        return Button.builder()
                .itemCreator(player -> {
                    String endFormat;
                    if (grant.isInfinite()) {
                        endFormat = "Never";
                    } else {
                        Date end = Date.from(Instant.ofEpochMilli(grant.getRunOutTimestamp()));
                        endFormat = DateFormat.getDateTimeInstance().format(end);
                    }

                    Date start = Date.from(Instant.ofEpochMilli(grant.start()));
                    List<String> lore = new ArrayList<>(List.of(
                            "",
                            "§eInvoked by: §f" + grant.sender(),
                            "§eReason: §f" + grant.reason(),
                            "",
                            "§eStart: §f" + DateUtil.formatDate(start),
                            "§eRuns out: §f" + endFormat
                    ));

                    if (player.isOp() || player.hasPermission("core.grant." + grant.rank().name().toLowerCase())) {
                        lore.add("");
                        lore.add("§7[Right -> Take away]");
                    }

                    Color javaColor = grant.rank().getJavaColor();
                    int red = javaColor.getRed(), green = javaColor.getGreen(), blue = javaColor.getBlue();
                    return new ItemBuilder(Material.LEATHER_CHESTPLATE)
                            .name(grant.rank().getDisplayName())
                            .color(org.bukkit.Color.fromRGB(red, green, blue))
                            .lore(lore)
                            .enchantment(new EnchantmentGlow(-1))
                            .build();
                }).eventConsumer(event -> {
                    if (!event.isRightClick()) {
                        return;
                    }
                    if (!event.getWhoClicked().isOp() && !event.getWhoClicked().hasPermission("core.grant." + grant.rank().name().toLowerCase())) {
                        return;
                    }
                    Player player = (Player) event.getWhoClicked();
                    player.closeInventory();
                    this.askForRemoveReason(player, grant);
                }).build();
    }

    private void askForRemoveReason(Player player, Grant grant) {
        ChatInput.request(player, "What is the reason for removing this grant?", List.of("Demoted", "Revoked"), response -> {
            player.sendMessage(MessageBuilder.construct("Removing grant..."));
            AlphaProfileManager.profiles()
                    .applyToDataAsync(this.targetID, profile ->
                            profile.removeGrant(grant, ProfileGrantRemoveEvent.Reason.TAKEN, player.getName(), response))
                    .thenRun(() -> openFor(player, this.targetID));
        });
    }

    private Button getHistoryIcon(GrantHistory.Entry entry) {
        return Button.builder()
                .itemCreator(player -> this.getHistoryItem(entry))
                .eventConsumer(event -> event.setCancelled(true))
                .build();
    }

    public ItemStack getHistoryItem(GrantHistory.Entry entry) {
        Grant.RankNameSnapshot grant = entry.grant();
        String scheduled = "Infinite";
        String actual;
        Date start = Date.from(Instant.ofEpochMilli(grant.start()));

        if (!grant.isInfinite()) {
            Date scheduledEnd = Date.from(Instant.ofEpochMilli(grant.getRunOutTimestamp()));
            Duration scheduledDuration = Duration.between(start.toInstant(), scheduledEnd.toInstant());
            scheduled = TimeUtil.formatIntoDetailedString((int) scheduledDuration.toSeconds());
        }

        Date actualEnd = Date.from(Instant.ofEpochMilli(entry.timestamp()));
        Duration actualDur = Duration.between(start.toInstant(), actualEnd.toInstant());
        actual = TimeUtil.formatIntoDetailedString((int) actualDur.toSeconds() + 1);

        List<String> lore = List.of(
                "",
                "§eInvoked by: §f" + grant.sender(),
                "§eReason: §f" + grant.reason(),
                "",
                "§eStart: §f" + DateUtil.formatDate(start),
                "§eEnd: §f" + DateUtil.formatDate(actualEnd),
                "§eScheduled Duration: §f" + scheduled,
                "§eActual Duration: §f" + actual,
                "",
                "§eRevoked by: §f" + entry.removedBy(),
                "§eRevoke reason: §f" + entry.removedReason(),
                ""
        );


        Color javaColor = Color.WHITE;
        String name = ChatColor.YELLOW + grant.rank() + " (Rank Removed)";
        Rank rank = grant.toRankEnum();
        if (rank != null) {
            javaColor = rank.getJavaColor();
            name = rank.getDisplayName();
        }

        int red = javaColor.getRed(), green = javaColor.getGreen(), blue = javaColor.getBlue();
        return new ItemBuilder(Material.LEATHER_CHESTPLATE)
                .name(name)
                .color(org.bukkit.Color.fromRGB(red, green, blue))
                .lore(lore)
                .build();
    }
}