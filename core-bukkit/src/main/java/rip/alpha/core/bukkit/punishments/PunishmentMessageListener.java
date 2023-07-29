package rip.alpha.core.bukkit.punishments;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import rip.alpha.core.bukkit.Core;
import rip.alpha.core.bukkit.common.ScheduledProfileTaskTracker;
import rip.alpha.core.shared.data.AlphaProfileUtilities;
import rip.alpha.core.shared.punishments.ProfilePunishmentAddEvent;
import rip.alpha.core.shared.punishments.ProfilePunishmentRemoveEvent;
import rip.alpha.core.shared.punishments.Punishment;
import rip.alpha.libraries.logging.LogLevel;
import rip.alpha.libraries.util.TimeUtil;
import rip.alpha.libraries.util.message.MessageBuilder;
import rip.alpha.libraries.util.message.MessageColor;
import rip.alpha.libraries.util.simple.SimpleText;
import rip.alpha.libraries.util.task.TaskUtil;

import java.util.concurrent.ExecutionException;

public class PunishmentMessageListener {
    public static void onPunishmentAdd(ProfilePunishmentAddEvent event) {
        Punishment punishment = event.punishment();
        Punishment.Type type = punishment.type();
        AlphaProfileUtilities.getColoredName(event.profileID()).thenAccept(name -> {
            try {
                String senderName = punishment.sender();
                if (!senderName.equalsIgnoreCase("CONSOLE")) {
                    senderName = AlphaProfileUtilities.getColoredName(senderName).get();
                } else {
                    senderName = MessageColor.RED + senderName;
                }

                String message = MessageBuilder
                        .standard("{} has been {} {} by {}.")
                        .prefix("Monitor")
                        .element(name)
                        .element(ChatColor.GOLD + (punishment.isInfinite() ? "permanently" : "temporarily"))
                        .element(ChatColor.GOLD + type.getParticiple().toLowerCase())
                        .element(senderName)
                        .build();
                SimpleText simpleText = new SimpleText(message);
                String hoverMessage = MessageBuilder
                        .standard("Duration: {}\nReason: {}")
                        .element(punishment.isInfinite() ? "Permanent" : TimeUtil.formatIntoDetailedString((int) (punishment.getTimeLeft() / 1000L)))
                        .element(punishment.reason())
                        .build();
                simpleText.hover(hoverMessage);
                BaseComponent[] messageComponents = simpleText.build();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.hasPermission("core.punishment.broadcast")) {
                        player.spigot().sendMessage(messageComponents);
                    }
                }
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }).thenRun(() -> {
            Player player = Bukkit.getPlayer(event.profileID());
            if (player == null) {
                return;
            }

            Core.LOGGER.log("Added punishment %s to %s".formatted(event.punishment().type(), event.profileID()), LogLevel.EXTENDED);

            String selfMessage;
            if (type == Punishment.Type.SERVER_WARN) {
                selfMessage = MessageBuilder
                        .error(type.getPunishedMessage())
                        .element(event.punishment().reason())
                        .build();
            } else {
                selfMessage = MessageBuilder
                        .error(type.getPunishedMessage())
                        .element(event.punishment().isInfinite() ? "permanently" : "temporarily")
                        .build();
            }

            player.sendMessage(selfMessage);
            player.playSound(player.getEyeLocation(), Sound.NOTE_PLING, 0.7F, 0.7F);
            handleEviction(punishment, player, selfMessage);
        });
    }

    public static void onPunishmentRemoved(ProfilePunishmentRemoveEvent event) {
        Punishment punishment = event.punishment();
        Punishment.Type type = punishment.type();

        AlphaProfileUtilities.getColoredName(event.profileID()).thenAccept(name -> {
            try {
                if (event.reason() == ProfilePunishmentRemoveEvent.Reason.LIFTED) {
                    String senderName = event.removedBy();
                    if (!senderName.equalsIgnoreCase("CONSOLE")) {
                        senderName = AlphaProfileUtilities.getColoredName(senderName).get();
                    } else {
                        senderName = MessageColor.RED + senderName;
                    }

                    String message = MessageBuilder
                            .standard("{} has been {} by {}.")
                            .prefix("Monitor")
                            .element(name)
                            .element(ChatColor.GOLD + ("un" + type.getParticiple().toLowerCase()))
                            .element(senderName)
                            .build();
                    Bukkit.broadcast(message, "core.punishment.broadcast");
                }
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }).thenRun(() -> {
            Player player = Bukkit.getPlayer(event.profileID());

            if (player == null) {
                return;
            }

            Core.LOGGER.log("Removed punishment %s from %s".formatted(event.punishment().type(), event.profileID()), LogLevel.EXTENDED);

            player.playSound(player.getEyeLocation(), Sound.NOTE_PLING, 0.7F, 0.7F);
            player.sendMessage(MessageBuilder.constructError(type.getExpiredMessage()));
        });
    }

    private static void handleEviction(Punishment punishment, Player player, String delegateMessage) {
        ScheduledProfileTaskTracker.addTask(player.getUniqueId(), punishment);
        if (punishment.type() == Punishment.Type.SERVER_BAN || punishment.type() == Punishment.Type.BLACKLIST) {
            TaskUtil.runSync(() -> player.kickPlayer(delegateMessage));
        }
    }
}
