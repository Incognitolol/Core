package rip.alpha.core.bukkit.modsuite.chat;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import rip.alpha.core.shared.data.AlphaProfileUtilities;
import rip.alpha.core.shared.server.NetworkServerHandler;
import rip.alpha.core.shared.server.event.NetworkServerREBroadcastEvent;
import rip.alpha.libraries.command.annotation.Command;
import rip.alpha.libraries.command.annotation.CommandUsage;
import rip.alpha.libraries.command.annotation.Wildcard;
import rip.alpha.libraries.util.TimeUtil;
import rip.alpha.libraries.util.message.MessageBuilder;
import rip.alpha.libraries.util.message.MessageColor;
import rip.alpha.libraries.util.message.MessageTranslator;

import java.time.Duration;
import java.util.function.Consumer;

public class ChatCommands {

    @CommandUsage("<message>")
    @Command(names = {"staffchat", "sc"}, permission = "core.command.staffchat", async = true)
    public static void onStaff(CommandSender sender, @Wildcard String message) {
        String serverName = NetworkServerHandler.getInstance().getCurrentServer().getDisplayName();
        if (sender instanceof Player player) {
            AlphaProfileUtilities.getColoredName(player.getUniqueId()).thenAccept(playerName ->
                    new StaffChatEvent(playerName, serverName, message).callEvent());
        } else {
            new StaffChatEvent(MessageColor.RED + "CONSOLE", serverName, message).callEvent();
        }
    }

    @CommandUsage("<message>")
    @Command(names = {"adminchat", "ac"}, permission = "core.command.adminchat", async = true)
    public static void onAdmin(CommandSender sender, @Wildcard String message) {
        String serverName = NetworkServerHandler.getInstance().getCurrentServer().getDisplayName();
        if (sender instanceof Player player) {
            AlphaProfileUtilities.getColoredName(player.getUniqueId()).thenAccept(playerName ->
                    new AdminChatEvent(playerName, serverName, message).callEvent());
        } else {
            new AdminChatEvent(MessageColor.RED + "CONSOLE", serverName, message).callEvent();
        }
    }

    @Command(names = {"clearchat", "cc"}, permission = "core.command.clearchat", async = true)
    public static void clearchat(CommandSender sender) {
        for (int i = 0; i < 700; i++) {
            for (Player target : Bukkit.getOnlinePlayers()) {
                if (!target.hasPermission("core.bypass.clearchat")) {
                    target.sendMessage(" ");
                }
            }
        }

        if (sender instanceof Player player) {
            AlphaProfileUtilities.getColoredName(player.getUniqueId()).thenAccept(name -> {
                String message = MessageTranslator.translate("&9[Staff] &7[%s] &r%s&r&7 cleared the chat".formatted(
                        NetworkServerHandler.getInstance().getCurrentServer().getDisplayName(), name));
                new NetworkServerREBroadcastEvent("bukkit", message, "core.staff.messages").callEvent();
            });
            return;
        }
        String message = MessageTranslator.translate("&9[Staff] &7[%s] &r%s&r&7 cleared the chat".formatted(
                NetworkServerHandler.getInstance().getCurrentServer().getDisplayName(), MessageColor.RED + "CONSOLE"));
        new NetworkServerREBroadcastEvent("bukkit", message, "core.staff.messages").callEvent();
    }

    @Command(names = {"mutechat"}, permission = "core.command.mutechat", async = true)
    public static void onMuteChat(CommandSender sender) {
        ChatManager manager = ChatManager.getInstance();
        manager.setMuteChat(!manager.isMuteChat());
        Bukkit.broadcastMessage(MessageBuilder.construct("Chat is now {}.", manager.isMuteChat() ? "disabled" : "enabled"));
    }

    @CommandUsage("<duration:5s>")
    @Command(names = {"slowchat"}, permission = "core.command.slowchat", async = true)
    public static void onSlowChat(CommandSender sender, Duration duration) {
        ChatManager manager = ChatManager.getInstance();

        if (duration.toMillis() < 0) {
            sender.sendMessage(MessageBuilder.constructError("You cannot do that duration for chat."));
            return;
        }

        manager.setMillisForSlowChat(duration.toMillis());
        if (manager.getMillisForSlowChat() > 0) {
            Bukkit.broadcastMessage(MessageBuilder.construct("Chat is now slowed by {}.", TimeUtil.formatLongIntoDetailedString(duration.toSeconds())));
        } else {
            Bukkit.broadcastMessage(MessageBuilder.construct("Chat is no-longer slowed."));
        }
    }

}
