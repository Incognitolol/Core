package rip.alpha.core.velocity.queue;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import rip.alpha.core.shared.queue.NetworkQueue;
import rip.alpha.core.shared.queue.NetworkQueueHandler;
import rip.alpha.core.shared.queue.event.PollQueueEvent;
import rip.alpha.core.velocity.Core;
import rip.alpha.libraries.util.message.MessageBuilder;

public class NetworkQueueListener {
    @Subscribe(order = PostOrder.LAST)
    public void onDisconnect(DisconnectEvent event) {
        NetworkQueueHandler.getInstance().leaveQueue(event.getPlayer().getUniqueId());
    }

    public static void handlePollQueue(PollQueueEvent event) {
        NetworkQueueHandler.getInstance().getQueueAsync(event.serverId())
                .thenApply(NetworkQueue::peek)
                .thenAccept(networkQueuePlayer -> handlePeek(event.serverId(), networkQueuePlayer));
    }

    private static void handlePeek(String serverId, NetworkQueue.NetworkQueuePlayer peekPlayer) {
        Core.getInstance().getProxyServer().getPlayer(peekPlayer.playerId()).ifPresent(player -> afterPeek(player, serverId));
    }

    private static void afterPeek(Player player, String serverId) {
        NetworkQueueHandler.getInstance().getQueueAsync(serverId)
                .thenApply(NetworkQueue::poll).thenAccept(networkQueuePlayer ->
                        Core.getInstance().getProxyServer().getServer(serverId)
                                .ifPresentOrElse(foundServer -> handleFoundServer(player, foundServer), () -> handleNotFoundServer(player, serverId)));
    }

    private static void handleFoundServer(Player player, RegisteredServer server) {
        player.createConnectionRequest(server).connect().thenAccept(result -> {
            switch (result.getStatus()) {
                case SUCCESS -> {
                    player.sendMessage(Component.text(MessageBuilder.construct("You have been connected to {}.", server.getServerInfo().getName())));
                }
                case ALREADY_CONNECTED -> {
                    player.sendMessage(Component.text(MessageBuilder.constructError("You were already connected to {}.", server.getServerInfo().getName())));
                }
                case CONNECTION_IN_PROGRESS -> {
                    player.sendMessage(Component.text(MessageBuilder.constructError("You were already connecting to {}.", server.getServerInfo().getName())));
                }
                default -> {
                    player.sendMessage(Component.text(MessageBuilder.constructError("Something went wrong while connecting to {}.", server.getServerInfo().getName())));
                }
            }
        });
    }

    private static void handleNotFoundServer(Player player, String serverId) {
        player.sendMessage(Component.text(MessageBuilder.constructError("Something went wrong while connecting to {}.", serverId)));
    }
}
