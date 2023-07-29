package rip.alpha.core.bukkit.punishments.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import rip.alpha.core.shared.data.AlphaProfile;
import rip.alpha.core.shared.data.AlphaProfileManager;
import rip.alpha.core.shared.discord.DiscordLogEvent;
import rip.alpha.core.shared.punishments.Punishment;
import rip.alpha.core.shared.server.NetworkServerHandler;
import rip.alpha.libraries.command.annotation.Command;
import rip.alpha.libraries.command.annotation.CommandUsage;
import rip.alpha.libraries.command.annotation.Wildcard;
import rip.alpha.libraries.util.message.MessageBuilder;

import java.awt.*;
import java.time.Duration;
import java.util.UUID;

public class WarnCommand {
    @CommandUsage("<target> <reason>")
    @Command(names = {"warn"}, permission = "core.command.warn", async = true)
    public static void onCommand(CommandSender sender, UUID targetID, @Wildcard String reason) {
        Punishment punishment = new Punishment(Punishment.Type.SERVER_WARN,
                sender instanceof Player senderPlayer ? senderPlayer.getName() : "CONSOLE", reason, Duration.ofMillis(-1).toMillis());
        AlphaProfileManager.profiles().applyToData(targetID, profile -> {
            if (sender instanceof Player player) {
                if (!player.isOp()) {
                    if (!AlphaProfileManager.profiles().isLocallyCached(player.getUniqueId())) {
                        sender.sendMessage(MessageBuilder.constructError("That player has a higher rank than you."));
                        String serverId = NetworkServerHandler.getInstance().getCurrentServer().getServerId();
                        String description = player.getName() + " tried to warn " + profile.getLastSeenName();
                        new DiscordLogEvent(serverId, "Disallowed action", description, Color.YELLOW).callEvent();
                        return;
                    }

                    AlphaProfile playerProfile = AlphaProfileManager.profiles().getCachedValue(player.getUniqueId());
                    if (playerProfile.getHighestRank().getPriority() >= profile.getHighestRank().getPriority()) {
                        sender.sendMessage(MessageBuilder.constructError("That player has a higher or equal rank than you."));
                        String serverId = NetworkServerHandler.getInstance().getCurrentServer().getServerId();
                        String description = player.getName() + " tried to warn " + profile.getLastSeenName();
                        new DiscordLogEvent(serverId, "Disallowed action", description, Color.YELLOW).callEvent();
                        return;
                    }
                }
            }

            profile.addPunishment(punishment);
        });
    }
}
