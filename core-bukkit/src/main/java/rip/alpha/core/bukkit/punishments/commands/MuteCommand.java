package rip.alpha.core.bukkit.punishments.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import rip.alpha.core.shared.data.AlphaProfile;
import rip.alpha.core.shared.data.AlphaProfileManager;
import rip.alpha.core.shared.discord.DiscordLogEvent;
import rip.alpha.core.shared.punishments.ProfilePunishmentRemoveEvent;
import rip.alpha.core.shared.punishments.Punishment;
import rip.alpha.core.shared.server.NetworkServerHandler;
import rip.alpha.libraries.command.annotation.Command;
import rip.alpha.libraries.command.annotation.CommandUsage;
import rip.alpha.libraries.command.annotation.Wildcard;
import rip.alpha.libraries.util.message.MessageBuilder;
import rip.alpha.libraries.util.uuid.UUIDFetcher;

import java.awt.*;
import java.time.Duration;
import java.util.UUID;

public class MuteCommand {
    @CommandUsage("<target> <duration> [reason...]")
    @Command(names = {"mute"}, permission = "core.command.mute", async = true)
    public static void onCommand(CommandSender sender, UUID playerID, Duration duration, @Wildcard String reason) {
        long time = duration.toMillis();
        if (!sender.hasPermission("core.command.mute.permanent") && (duration.toDays() > 30 || time == -1)) {
            sender.sendMessage(MessageBuilder.constructError("You do not have permission to mute players for that duration."));
            return;
        }

        String targetName = UUIDFetcher.getName(playerID);

        AlphaProfileManager.profiles().applyToData(playerID, profile -> {
            if (profile.hasPunishment(Punishment.Type.SERVER_MUTE)) {
                sender.sendMessage(MessageBuilder.constructError("{} is already muted.", targetName));
                return;
            }

            if (sender instanceof Player player) {
                if (!player.isOp()) {
                    if (!AlphaProfileManager.profiles().isLocallyCached(player.getUniqueId())) {
                        sender.sendMessage(MessageBuilder.constructError("That player has a higher rank than you."));
                        String serverId = NetworkServerHandler.getInstance().getCurrentServer().getServerId();
                        String description = player.getName() + " tried to mute " + targetName;
                        new DiscordLogEvent(serverId, "Disallowed action", description, Color.YELLOW).callEvent();
                        return;
                    }

                    AlphaProfile playerProfile = AlphaProfileManager.profiles().getCachedValue(player.getUniqueId());
                    if (playerProfile.getHighestRank().getPriority() >= profile.getHighestRank().getPriority()) {
                        sender.sendMessage(MessageBuilder.constructError("That player has a higher or equal rank than you."));
                        String serverId = NetworkServerHandler.getInstance().getCurrentServer().getServerId();
                        String description = player.getName() + " tried to mute " + targetName;
                        new DiscordLogEvent(serverId, "Disallowed action", description, Color.YELLOW).callEvent();
                        return;
                    }
                }
            }

            Punishment punishment = new Punishment(Punishment.Type.SERVER_MUTE,
                    sender instanceof Player senderPlayer ? senderPlayer.getName() : "CONSOLE", reason, time);
            profile.addPunishment(punishment);
        });
    }

    @CommandUsage("<target> [reason...]")
    @Command(names = {"unmute"}, permission = "core.command.unmute", async = true)
    public static void onCommand(CommandSender sender, UUID targetID, @Wildcard String reason) {
        String targetName = UUIDFetcher.getName(targetID);
        if (!AlphaProfileManager.profiles().exists(targetID)) {
            sender.sendMessage(MessageBuilder.constructError("Couldn't find a profile with the name '{}'", targetName));
            return;
        }

        String senderName = sender instanceof Player ? sender.getName() : "CONSOLE";
        AlphaProfileManager.profiles().applyToData(targetID, profile -> {
            if (!profile.removePunishmentForType(Punishment.Type.SERVER_MUTE, ProfilePunishmentRemoveEvent.Reason.LIFTED, senderName, reason)) {
                sender.sendMessage(MessageBuilder.constructError("{} is currently not muted.", targetName));
            }
        });
    }
}
