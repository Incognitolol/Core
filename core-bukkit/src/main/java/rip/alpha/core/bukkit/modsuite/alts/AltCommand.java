package rip.alpha.core.bukkit.modsuite.alts;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import rip.alpha.core.shared.data.IpUuidCache;
import rip.alpha.libraries.command.annotation.Command;
import rip.alpha.libraries.command.annotation.CommandUsage;
import rip.alpha.libraries.util.data.NameCache;
import rip.alpha.libraries.util.message.MessageBuilder;

import java.util.UUID;

public class AltCommand {

    @CommandUsage("<target>")
    @Command(names = {"alts", "alt"}, async = true, permission = "core.command.alts")
    public static void onCommand(CommandSender sender, String target) {
        AltsFetcher.fetchAltNames(target, sender::sendMessage);
    }

    @CommandUsage("<target>")
    @Command(names = {"ipclear"}, async = true, permission = "core.command.ipclear")
    public static void onClear(CommandSender sender, UUID target) {
        sender.sendMessage(MessageBuilder.construct("Cleaning data..."));
        Player player = Bukkit.getPlayer(target);
        if (player != null) {
            player.kickPlayer(MessageBuilder.construct("Your data is getting cleaned."));
        }
        IpUuidCache cache = IpUuidCache.getInstance();
        cache.getIpsOf(target).addresses().forEach(ip -> cache.removeIdFromIp(target, ip));
        cache.clearIpsOf(target);
        String name = NameCache.getInstance().getName(target);
        sender.sendMessage(MessageBuilder.construct("Cleared all IPs of {}", name));
    }

}
