package rip.alpha.core.bukkit.server.command;

import org.bukkit.command.CommandSender;
import rip.alpha.core.shared.server.NetworkServer;
import rip.alpha.core.shared.server.NetworkServerHandler;
import rip.alpha.libraries.command.annotation.Command;
import rip.alpha.libraries.util.message.MessageBuilder;
import rip.alpha.libraries.util.message.MessageColor;
import rip.alpha.libraries.util.message.MessageTranslator;

public class NetworkServerDumpCommand {
    @Command(names = {"networkserver dump", "ns dump"}, permission = "core.command.networkdump", async = true)
    public static void networkServersListCommand(CommandSender commandSender) {
        NetworkServerHandler.getInstance().getServersAsync().thenAccept(networkServers -> {
            commandSender.sendMessage(MessageTranslator.translate("&e&lNetwork &eServer Dump"));

            NetworkServer networkServer = NetworkServerHandler.getInstance().getCurrentServer();
            commandSender.sendMessage(MessageTranslator.translate("&eType: &d" + networkServer.getServerType().name()));
            commandSender.sendMessage(MessageTranslator.translate("&ePlatform: &d" + networkServer.getServerPlatform().name()));
            commandSender.sendMessage(MessageTranslator.translate("&eStatus: &d" + networkServer.getNetworkServerStatus().name()));

            String message = new MessageBuilder(MessageColor.YELLOW, MessageColor.LIGHT_PURPLE, MessageColor.YELLOW, MessageColor.GRAY)
                    .message("Linked to {} other server(s).")
                    .element(networkServers.size())
                    .build();

            commandSender.sendMessage(message);
        });
    }
}
