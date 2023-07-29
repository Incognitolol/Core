package rip.alpha.core.bukkit.essentials;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import rip.alpha.core.bukkit.data.player.CorePlayerProfile;
import rip.alpha.core.bukkit.data.player.CorePlayerProfileManager;
import rip.alpha.core.shared.data.AlphaProfile;
import rip.alpha.core.shared.data.AlphaProfileManager;
import rip.alpha.core.shared.data.AlphaProfileUtilities;
import rip.alpha.core.shared.ranks.Rank;
import rip.alpha.libraries.command.annotation.Command;
import rip.alpha.libraries.command.annotation.CommandUsage;
import rip.alpha.libraries.command.annotation.Wildcard;
import rip.alpha.libraries.util.message.MessageBuilder;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class MessageCommands {

    @CommandUsage("<target> <message>")
    @Command(names = {"msg", "message", "m"}, async = true)
    public static void onMessage(Player sender, Player target, @Wildcard String message) throws ExecutionException, InterruptedException {
        CorePlayerProfile senderProfile = CorePlayerProfileManager.profiles().getData(sender.getUniqueId());
        senderProfile.setLastMessagedID(target.getUniqueId());
        senderProfile.setLastMessagedName(target.getName());
        CorePlayerProfile targetProfile = CorePlayerProfileManager.profiles().getData(target.getUniqueId());
        targetProfile.setLastMessagedID(sender.getUniqueId());
        targetProfile.setLastMessagedName(sender.getName());

        Rank senderRank = AlphaProfileUtilities.getRank(sender.getUniqueId()).get();
        Rank targetRank = AlphaProfileUtilities.getRank(target.getUniqueId()).get();

        String formatted = MessageBuilder.construct("{} -> {}: {}", senderRank.getColor() + sender.getName(), ChatColor.GOLD + "You", message);
        target.sendMessage(formatted);
        target.playSound(target.getEyeLocation(), Sound.NOTE_PLING, 0.5F, 1.2F);
        String formattedS = MessageBuilder.construct("{} -> {}: {}", ChatColor.GOLD + "You", targetRank.getColor() + target.getName(), message);
        sender.sendMessage(formattedS);
    }

    @CommandUsage("<message>")
    @Command(names = {"r", "reply", "respond"}, async = true)
    public static void onReply(Player sender, @Wildcard String message) throws ExecutionException, InterruptedException {
        CorePlayerProfile corePlayerProfile = CorePlayerProfileManager.profiles().getData(sender.getUniqueId());
        UUID lastMessagedID = corePlayerProfile.getLastMessagedID();
        if (lastMessagedID == null) {
            sender.sendMessage(MessageBuilder.constructError("You dont have any recent messages."));
            return;
        }
        String lastMessagedName = corePlayerProfile.getLastMessagedName();
        Player target = Bukkit.getPlayer(lastMessagedID);
        if (target == null) {
            sender.sendMessage(MessageBuilder.constructError("{} is currently offline.", lastMessagedName));
            return;
        }
        onMessage(sender, target, message);
    }

}
