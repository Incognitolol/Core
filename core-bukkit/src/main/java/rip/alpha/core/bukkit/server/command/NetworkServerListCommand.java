package rip.alpha.core.bukkit.server.command;

import org.bukkit.entity.Player;
import rip.alpha.core.bukkit.server.menu.NetworkServersListMenu;
import rip.alpha.core.shared.server.NetworkServerHandler;
import rip.alpha.libraries.command.annotation.Command;
import rip.alpha.libraries.util.task.TaskUtil;

public class NetworkServerListCommand {
    @Command(names = {"networkserver list", "ns list", "servers"}, permission = "core.command.networklist")
    public static void networkServersListCommand(Player player) {
        NetworkServerHandler.getInstance().getServersAsync().thenAccept(networkServers ->
                TaskUtil.runSync(() -> new NetworkServersListMenu(networkServers).open(player)));
    }
}
