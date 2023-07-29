package rip.alpha.core.bukkit.punishments;

import net.minecraft.server.v1_7_R4.EnchantmentGlow;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import rip.alpha.core.shared.data.AlphaProfile;
import rip.alpha.core.shared.data.AlphaProfileManager;
import rip.alpha.core.shared.data.PunishmentHistory;
import rip.alpha.core.shared.punishments.ProfilePunishmentRemoveEvent;
import rip.alpha.core.shared.punishments.Punishment;
import rip.alpha.libraries.chatinput.ChatInput;
import rip.alpha.libraries.gui.Button;
import rip.alpha.libraries.gui.PaginatedMenu;
import rip.alpha.libraries.util.DateUtil;
import rip.alpha.libraries.util.TimeUtil;
import rip.alpha.libraries.util.item.ItemBuilder;
import rip.alpha.libraries.util.message.MessageBuilder;
import rip.alpha.libraries.util.task.TaskUtil;

import java.text.DateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class PunishmentMenu extends PaginatedMenu {

    private final List<Punishment> activePunishments;
    private final PunishmentHistory punishmentHistory;
    private final UUID targetID;

    public static void openFor(Player player, UUID targetID) {
        player.sendMessage(MessageBuilder.construct("Loading..."));
        AlphaProfileManager.profiles().getOrCreateRealTimeDataAsync(targetID).thenAccept(profile ->
                TaskUtil.runSync(() -> openFor(player, profile)));
    }

    public static void openFor(Player player, AlphaProfile profile) {
        new PunishmentMenu(profile.getAllActivePunishments(), profile.getPunishmentHistory(), profile.getMojangID()).open(player);
    }

    public PunishmentMenu(List<Punishment> activePunishments, PunishmentHistory punishmentHistory, UUID targetID) {
        super("&6Punishments", 4 * 9);
        this.activePunishments = activePunishments;
        this.punishmentHistory = punishmentHistory;
        this.targetID = targetID;
    }

    @Override
    protected void setupPaginatedMenu(HumanEntity player) {
        ListIterator<Punishment> activePunishmentIterator = this.activePunishments.listIterator(this.activePunishments.size());
        while (activePunishmentIterator.hasPrevious()) {
            this.addPageElement(this.getPunishmentIcon(activePunishmentIterator.previous()));
        }

        ListIterator<PunishmentHistory.Entry> grantHistoryIterator = this.punishmentHistory.getEntries().listIterator(this.punishmentHistory.getEntries().size());
        while (grantHistoryIterator.hasPrevious()) {
            this.addPageElement(this.getHistoryIcon(grantHistoryIterator.previous()));
        }
    }

    private Button getPunishmentIcon(Punishment punishment) {
        return Button.builder()
                .itemCreator(player -> {
                    String endFormat;
                    if (punishment.isInfinite()) {
                        endFormat = "Never";
                    } else {
                        Date end = Date.from(Instant.ofEpochMilli(punishment.getRunOutTimestamp()));
                        endFormat = DateFormat.getDateTimeInstance().format(end);
                    }

                    Material material = switch (punishment.type()) {
                        case BLACKLIST -> Material.COAL_BLOCK;
                        case SERVER_MUTE -> Material.WRITTEN_BOOK;
                        case SERVER_BAN -> Material.ANVIL;
                        case SERVER_WARN -> Material.PAPER;
                    };

                    Date start = Date.from(Instant.ofEpochMilli(punishment.start()));
                    List<String> lore = new ArrayList<>(List.of(
                            "",
                            "§eInvoked by: §f" + punishment.sender(),
                            "§eReason: §f" + punishment.reason(),
                            "",
                            "§eStart: §f" + DateUtil.formatDate(start),
                            "§eRuns out: §f" + endFormat,
                            ""
                    ));

                    if (player.hasPermission("core.command.un" + punishment.type().getTypeName().toLowerCase())) {
                        lore.add("§7[Right -> Take away]");
                    }

                    return new ItemBuilder(material)
                            .name("§6" + punishment.type().getTypeName())
                            .lore(lore)
                            .enchantment(new EnchantmentGlow(-1))
                            .build();
                }).eventConsumer(event -> {
                    if (!event.isRightClick()) {
                        return;
                    }
                    Player player = (Player) event.getWhoClicked();
                    if (!player.hasPermission("core.command.un" + punishment.type().getTypeName().toLowerCase())) {
                        return;
                    }
                    player.closeInventory();
                    this.askForRemoveReason(player, punishment);
                }).build();
    }

    private void askForRemoveReason(Player player, Punishment punishment) {
        List<String> suggestions = new ArrayList<>();
        suggestions.add("Reduced");
        suggestions.add("Appealed");

        if (punishment.type() == Punishment.Type.SERVER_BAN) {
            suggestions.add("Unban #1");
            suggestions.add("Unban #2");
            suggestions.add("Unban #3");
        }

        ChatInput.request(player, "What is the reason for removing this grant?", suggestions, response -> {
            player.sendMessage(MessageBuilder.construct("Removing grant..."));
            AlphaProfileManager.profiles()
                    .applyToDataAsync(this.targetID, profile ->
                            profile.removePunishment(punishment, ProfilePunishmentRemoveEvent.Reason.LIFTED, player.getName(), response))
                    .thenRun(() -> openFor(player, this.targetID));
        });
    }

    private Button getHistoryIcon(PunishmentHistory.Entry entry) {
        return Button.builder()
                .itemCreator(player -> {
                    Punishment punishment = entry.punishment();

                    String scheduled;
                    Date start = Date.from(Instant.ofEpochMilli(punishment.start()));
                    if (punishment.isInfinite()) {
                        scheduled = "Infinite";
                    } else {
                        Date scheduledEnd = Date.from(Instant.ofEpochMilli(punishment.getRunOutTimestamp()));
                        Duration scheduledDuration = Duration.between(start.toInstant(), scheduledEnd.toInstant());
                        scheduled = TimeUtil.formatIntoDetailedString((int) scheduledDuration.toSeconds());
                    }

                    Date actualEnd = Date.from(Instant.ofEpochMilli(entry.timestamp()));
                    Duration actualDur = Duration.between(start.toInstant(), actualEnd.toInstant());
                    String actual = TimeUtil.formatIntoDetailedString((int) actualDur.toSeconds() + 1);

                    Material material = switch (punishment.type()) {
                        case BLACKLIST -> Material.COAL_BLOCK;
                        case SERVER_MUTE -> Material.BOOK;
                        case SERVER_BAN -> Material.ANVIL;
                        case SERVER_WARN -> Material.PAPER;
                    };

                    List<String> lore = new ArrayList<>(List.of(
                            "",
                            "§eInvoked by: §f" + punishment.sender(),
                            "§eReason: §f" + punishment.reason(),
                            "",
                            "§eStart: §f" + DateUtil.formatDate(start),
                            "§eEnd: §f" + DateUtil.formatDate(actualEnd),
                            "§eScheduled Duration: §f" + scheduled,
                            "§eActual Duration: §f" + actual,
                            ""
                    ));

                    if (punishment.type() != Punishment.Type.SERVER_WARN) {
                        lore.add("§eRevoked by: §f" + entry.removedBy());
                        lore.add("§eRevoke reason: §f" + entry.removedReason());
                        lore.add("");
                    }

                    return new ItemBuilder(material)
                            .name("§6" + punishment.type().getParticiple())
                            .lore(lore)
                            .build();
                }).eventConsumer(event -> {

                }).build();
    }

}
