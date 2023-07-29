package rip.alpha.core.bukkit.grants;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import rip.alpha.core.bukkit.Core;
import rip.alpha.core.bukkit.common.ScheduledProfileTaskTracker;
import rip.alpha.core.shared.data.AlphaProfileUtilities;
import rip.alpha.core.shared.grants.Grant;
import rip.alpha.core.shared.grants.ProfileGrantAddEvent;
import rip.alpha.core.shared.grants.ProfileGrantRemoveEvent;
import rip.alpha.core.shared.ranks.Rank;
import rip.alpha.libraries.logging.LogLevel;
import rip.alpha.libraries.util.TimeUtil;
import rip.alpha.libraries.util.message.MessageBuilder;
import rip.alpha.libraries.util.message.MessageColor;
import rip.alpha.libraries.util.simple.SimpleText;

import java.util.concurrent.ExecutionException;

public class GrantMessageListener {
    public static void onGrantGained(ProfileGrantAddEvent event) {
        Grant grant = event.grant();
        Rank rank = event.grant().rank();

        if (rank == null) {
            Core.LOGGER.severe("Tried to broadcast a message for a NULL rank", LogLevel.BASIC);
            return;
        }

        AlphaProfileUtilities.getColoredName(event.profileID()).thenAccept(name -> {
            try {
                String senderName = grant.sender();
                if (!senderName.equalsIgnoreCase("CONSOLE")) {
                    senderName = AlphaProfileUtilities.getColoredName(senderName).get();
                } else {
                    senderName = MessageColor.RED + senderName;
                }

                String message = MessageBuilder
                        .standard("{} has been {} granted {} by {}.")
                        .prefix("Monitor")
                        .element(name)
                        .element(ChatColor.GOLD + (grant.isInfinite() ? "permanently" : "temporarily"))
                        .element(rank.getDisplayName())
                        .element(senderName)
                        .build();
                SimpleText simpleText = new SimpleText(message);
                String hoverMessage = MessageBuilder
                        .standard("Duration: {}\nReason: {}\nRank: {}")
                        .element(grant.isInfinite() ? "Permanent" : TimeUtil.formatIntoDetailedString((int) (grant.getTimeLeft() / 1000L)))
                        .element(grant.reason())
                        .element(rank.getDisplayName())
                        .build();
                simpleText.hover(hoverMessage);
                BaseComponent[] messageComponents = simpleText.build();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.hasPermission("core.grants.broadcast")) {
                        player.spigot().sendMessage(messageComponents);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).thenRun(() -> {
            Player player = Bukkit.getPlayer(event.profileID());

            if (player == null) {
                return;
            }

            Core.LOGGER.log("Added %s to %s".formatted(grant, event.profileID()), LogLevel.VERBOSE);
            RankPermissionEvaluator.reEvaluatePermissionsRealTime(player);
            String selfMessage = MessageBuilder
                    .standard("You've been {} granted {} rank.")
                    .element(ChatColor.GOLD + (grant.isInfinite() ? "permanently" : "temporarily"))
                    .element(rank.getDisplayName())
                    .build();
            String duration = grant.isInfinite() ? "Permanent" : TimeUtil.formatIntoDetailedString((int) (grant.getTimeLeft() / 1000L));
            String hoverMessage = MessageBuilder.construct("Duration: {}.", duration);
            new SimpleText(selfMessage).hover(hoverMessage).send(player);
            ScheduledProfileTaskTracker.addTask(player.getUniqueId(), grant);
        });
    }

    public static void onGrantLost(ProfileGrantRemoveEvent event) {
        Grant grant = event.grant();
        Rank rank = event.grant().rank();
        AlphaProfileUtilities.getColoredName(event.profileID()).thenAccept(name -> {
            try {
                if (event.reason() == ProfileGrantRemoveEvent.Reason.TAKEN) {
                    String senderName = event.removedBy();
                    if (!senderName.equalsIgnoreCase("CONSOLE")) {
                        senderName = AlphaProfileUtilities.getColoredName(senderName).get();
                    } else {
                        senderName = MessageColor.RED + senderName;
                    }

                    String message = MessageBuilder
                            .standard("{} has been un-granted {} by {}.")
                            .prefix("Monitor")
                            .element(name)
                            .element(rank.getDisplayName())
                            .element(senderName)
                            .build();
                    Bukkit.broadcast(message, "core.grants.broadcast");
                }
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }).thenRun(() -> {
            Player player = Bukkit.getPlayer(event.profileID());

            if (player == null) {
                return;
            }

            Core.LOGGER.log("Removed %s from %s".formatted(grant, event.profileID()), LogLevel.VERBOSE);
            RankPermissionEvaluator.reEvaluatePermissionsRealTime(player);

            if (event.reason() == ProfileGrantRemoveEvent.Reason.REPLACED) {
                return;
            }

            player.sendMessage(MessageBuilder.construct("Your grant for {} has expired.", rank.getDisplayName()));
        });
    }
}
