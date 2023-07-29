package rip.alpha.core.bukkit.modsuite.requests;

import org.bukkit.entity.Player;
import rip.alpha.core.bukkit.data.server.GlobalServerDataManager;
import rip.alpha.core.bukkit.modsuite.ModerationLocation;
import rip.alpha.core.shared.server.NetworkServerHandler;
import rip.alpha.libraries.command.annotation.Command;
import rip.alpha.libraries.command.annotation.CommandUsage;
import rip.alpha.libraries.command.annotation.Wildcard;
import rip.alpha.libraries.util.message.MessageBuilder;

import java.util.UUID;

public class RequestsCommands {

    @CommandUsage("<reason>")
    @Command(names = {"request"}, async = true)
    public static void onRequest(Player sender, @Wildcard String reason) {
        sender.sendMessage(MessageBuilder.construct("Sending request..."));
        UUID repID = sender.getUniqueId();
        String repName = sender.getName();
        ModerationLocation loc = ModerationLocation.of(sender.getLocation());
        String serverID = NetworkServerHandler.getInstance().getCurrentServer().getServerId();
        Request request = new Request(repID, repName, loc, serverID, reason);
        GlobalServerDataManager.servers()
                .applyToDataAsync(serverID, data -> data.getRequestsData().addRequest(request))
                .thenRun(() -> new PlayerRequestHelpEvent(request).callEvent())
                .thenRun(() -> sender.sendMessage(MessageBuilder.construct("Request was sent.")));
    }

    @Command(names = {"requests", "requestmenu"}, permission = "core.command.requests")
    public static void onRequestMenu(Player sender) {
        RequestMenu.openFor(sender);
    }

}
