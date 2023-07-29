package rip.alpha.core.bukkit.modsuite.modmode;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import rip.alpha.core.bukkit.data.player.CorePlayerProfile;
import rip.alpha.core.bukkit.data.player.CorePlayerProfileManager;
import rip.alpha.core.bukkit.modsuite.chat.StaffChatEvent;
import rip.alpha.core.bukkit.modsuite.modmode.ModModeEntry;
import rip.alpha.core.bukkit.modsuite.modmode.ModModeEntryCache;
import rip.alpha.core.bukkit.modsuite.modmode.ModModeHandler;
import rip.alpha.core.bukkit.modsuite.modmode.ModModeStatus;
import rip.alpha.core.shared.data.AlphaProfileUtilities;
import rip.alpha.core.shared.server.NetworkServerHandler;
import rip.alpha.core.shared.server.event.NetworkServerREBroadcastEvent;
import rip.alpha.libraries.command.annotation.Command;
import rip.alpha.libraries.command.annotation.CommandUsage;
import rip.alpha.libraries.util.message.MessageColor;
import rip.alpha.libraries.util.message.MessageTranslator;

public class ModModeCommand {
    @Command(names = {"modsuite", "mod", "modmode", "hackermode", "hm", "h"}, permission = "core.command.modmode")
    public static void modModeCommand(Player player) {
        ModModeEntry entry = ModModeEntryCache.getInstance().computeIfAbsent(player);

        switch (entry.getModModeStatus()) {
            case NONE -> {
                ModModeStatus modeStatus = player.hasPermission("core.modmode.admin") ? ModModeStatus.ADMIN : ModModeStatus.MOD;
                ModModeHandler.getInstance().changeStatus(player, modeStatus);
            }
            case ADMIN, MOD -> {
                ModModeHandler.getInstance().changeStatus(player, ModModeStatus.NONE);
            }
        }
    }
    @Command(names = {"freeze", "ss"}, permission = "core.command.freeze", async = true)
    @CommandUsage("<Target>")
    public static void freezeCommand(Player player, Player target) {
        if (player.getUniqueId().equals(target.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You may not freeze yourself.");
            return;
        }
        ModModeEntry targetEntry = ModModeEntryCache.getInstance().computeIfAbsent(target);
        if(targetEntry.isFrozen()) {
            player.sendMessage(ChatColor.RED + "That player is already frozen");
            return;
        }
        targetEntry.setFrozen(true);

        AlphaProfileUtilities.getColoredName(player.getUniqueId()).thenAccept(playerName -> {
            String message = MessageTranslator.translate("&9[Staff] &7[%s] &r%s&r&9 froze &c%s"
                    .formatted(NetworkServerHandler.getInstance().getCurrentServer().getDisplayName(), playerName, target.getName()));
            new NetworkServerREBroadcastEvent("bukkit", message, "core.staff.messages").callEvent();
        });
    }
    @Command(names = {"unfreeze"}, permission = "core.command.freeze", async = true)
    @CommandUsage("<Target>")
    public static void unfreezeCommand(Player player, Player target) {
        ModModeEntry targetEntry = ModModeEntryCache.getInstance().computeIfAbsent(target);
        if(!targetEntry.isFrozen()) {
            player.sendMessage(ChatColor.RED + "That player isn't frozen");
            return;
        }
        targetEntry.setFrozen(false);
        player.sendMessage(ChatColor.GREEN + "YOu have unfrozen " + ChatColor.RED + target.getName());
    }
}
