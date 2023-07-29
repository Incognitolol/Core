package rip.alpha.core.bukkit.essentials;

import org.bukkit.command.CommandSender;
import rip.alpha.core.shared.server.NetworkServer;
import rip.alpha.core.shared.server.NetworkServerEntity;
import rip.alpha.core.shared.server.event.NetworkServerSendAllEvent;
import rip.alpha.core.shared.server.event.NetworkServerSendPlayerEvent;
import rip.alpha.core.shared.server.event.NetworkServerSendServerEvent;
import rip.alpha.libraries.command.annotation.Command;
import rip.alpha.libraries.command.annotation.CommandUsage;
import rip.alpha.libraries.util.message.MessageBuilder;

public class SendCommand {
    @CommandUsage("<target> <toServer>")
    @Command(names = {"sendplayer", "send"}, permission = "core.command.send", async = true)
    public static void sendCommand(CommandSender sender, NetworkServerEntity entity, NetworkServer.NetworkServerSnapshot toServer) {
        new NetworkServerSendPlayerEvent(entity.entityId(), toServer.getServerId()).callEvent();
        sender.sendMessage(MessageBuilder.construct("Sending {} to {}...", entity.entityName(), toServer.getServerId()));
    }

    @CommandUsage("<toServer>")
    @Command(names = {"sendall"}, permission = "core.command.sendall", async = true)
    public static void sendAllCommand(CommandSender sender, NetworkServer.NetworkServerSnapshot toServer) {
        new NetworkServerSendAllEvent(toServer.getServerId()).callEvent();
        sender.sendMessage(MessageBuilder.construct("Sending all players to {}...", toServer.getServerId()));
    }

    @CommandUsage("<fromServer> <toServer>")
    @Command(names = {"sendserver"}, permission = "core.command.sendserver", async = true)
    public static void sendAllCommand(CommandSender sender, NetworkServer.NetworkServerSnapshot fromServer, NetworkServer.NetworkServerSnapshot toServer) {
        new NetworkServerSendServerEvent(fromServer.getServerId(), toServer.getServerId()).callEvent();
        sender.sendMessage(MessageBuilder.construct("Sending all players on {} to {}...", fromServer.getServerId(), toServer.getServerId()));
    }
}
