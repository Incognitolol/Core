package rip.alpha.core.bukkit.essentials;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import rip.alpha.core.shared.server.NetworkServer;
import rip.alpha.core.shared.server.NetworkServerType;
import rip.alpha.libraries.command.annotation.Command;
import rip.alpha.libraries.command.annotation.CommandUsage;
import rip.alpha.libraries.util.BungeeUtil;
import rip.alpha.libraries.util.message.MessageBuilder;

public class ServerCommand {
    @CommandUsage("<server>")
    @Command(names = {"server"}, async = true, permission = "core.command.server")
    public static void onServerCommand(Player player, NetworkServer.NetworkServerSnapshot networkServerSnapshot) {
        if(networkServerSnapshot.getServerType() == NetworkServerType.DEVELOPMENT) {
            if(!player.hasPermission("core.command.server.type.development")) {
                player.sendMessage(ChatColor.RED + "No Permission");
                return;
            }
        }
        player.sendMessage(MessageBuilder.construct("You are now being sent to {}...", networkServerSnapshot.getServerId()));
        BungeeUtil.sendToServer(player, networkServerSnapshot.getServerId());
    }
}
