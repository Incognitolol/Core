package rip.alpha.core.bukkit.queue;

import org.bukkit.entity.Player;
import rip.alpha.core.shared.queue.NetworkQueueHandler;
import rip.alpha.core.shared.server.NetworkServer;
import rip.alpha.core.shared.server.NetworkServerPlatform;
import rip.alpha.libraries.command.annotation.Command;
import rip.alpha.libraries.util.message.MessageBuilder;

public class NetworkQueueCommands {
    @Command(names = {"joinqueue"}, async = true)
    public static void joinQueueCommand(Player player, NetworkServer.NetworkServerSnapshot snapshot) {
        if (snapshot.getServerPlatform() != NetworkServerPlatform.BUKKIT) {
            player.sendMessage(MessageBuilder.constructError("You cannot queue for that type of server."));
            return;
        }

        if (snapshot.getServerType().isHub()) {
            player.sendMessage(MessageBuilder.constructError("You cannot queue for a hub server."));
            return;
        }

        NetworkQueueHandler handler = NetworkQueueHandler.getInstance();

        handler.inQueue(player.getUniqueId()).thenAccept(inQueue -> {
            if (inQueue) {
                player.sendMessage(MessageBuilder.constructError("You are already in a queue."));
                return;
            }

            handler.joinQueue(player.getUniqueId(), snapshot.getServerId())
                    .thenAccept(position -> {
                        String message = MessageBuilder.standard("You have joined the queue for {} at position {}.")
                                .element(snapshot.getServerId())
                                .element("#" + position)
                                .build();
                        player.sendMessage(message);
                    });
        });
    }

    @Command(names = {"leavequeue"}, async = true)
    public static void leaveQueueCommand(Player player) {
        NetworkQueueHandler.getInstance().leaveQueue(player.getUniqueId()).thenRun(() ->
                player.sendMessage(MessageBuilder.construct("You have left your queue."))
        );
    }
}
