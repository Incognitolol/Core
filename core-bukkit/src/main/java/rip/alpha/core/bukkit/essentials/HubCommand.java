package rip.alpha.core.bukkit.essentials;

import org.bukkit.entity.Player;
import rip.alpha.core.shared.server.NetworkServerHandler;
import rip.alpha.libraries.command.annotation.Command;
import rip.alpha.libraries.util.BungeeUtil;
import rip.alpha.libraries.util.message.MessageBuilder;

public class HubCommand {
    @Command(names = {"hub", "lobby"}, async = true)
    public static void hubCommand(Player player) {
        if (NetworkServerHandler.getInstance().getCurrentServer().getServerType().isHub()) {
            player.sendMessage(MessageBuilder.constructError("You are already connected to a lobby."));
            return;
        }
        player.sendMessage(MessageBuilder.construct("Transferring you to a lobby..."));
        BungeeUtil.sendToServer(player, "lobby");
    }
}
