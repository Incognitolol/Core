package rip.alpha.core.velocity.listener;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import rip.alpha.core.shared.data.AlphaProfileUtilities;
import rip.alpha.core.shared.server.event.NetworkServerREBroadcastEvent;
import rip.alpha.libraries.util.message.MessageBuilder;
import rip.alpha.libraries.util.message.MessageColor;

public class ConnectionListener {
    @Subscribe(order = PostOrder.LAST)
    public void onServerConnected(ServerConnectedEvent event) {
        Player player = event.getPlayer();

        if (!player.hasPermission("core.staff.messages")) {
            return;
        }

        AlphaProfileUtilities.getColoredName(player.getUniqueId()).thenAccept(coloredName -> {
            event.getPreviousServer().ifPresentOrElse(previousServer -> {
                String message = this.getMessageBuilder("{} has switched from {} to {}.")
                        .element(coloredName)
                        .element(previousServer.getServerInfo().getName())
                        .element(event.getServer().getServerInfo().getName())
                        .build();
                this.broadcastToStaff(message);
            }, () -> {
                String message = this.getMessageBuilder("{} has joined the network. ({})")
                        .element(coloredName)
                        .element(event.getServer().getServerInfo().getName())
                        .build();
                this.broadcastToStaff(message);
            });
        });
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        Player player = event.getPlayer();

        if (!player.hasPermission("core.staff.messages")) {
            return;
        }

        AlphaProfileUtilities.getColoredName(player.getUniqueId()).thenAccept(coloredName -> {
            event.getPlayer().getCurrentServer().ifPresent(serverConnection -> {
                String message = this.getMessageBuilder("{} has left the network. ({})")
                        .element(coloredName)
                        .element(serverConnection.getServerInfo().getName())
                        .build();
                this.broadcastToStaff(message);
            });
        });
    }

    private void broadcastToStaff(String message) {
        new NetworkServerREBroadcastEvent("bukkit", message, "core.staff.messages").callEvent();
    }

    private MessageBuilder getMessageBuilder(String message) {
        return new MessageBuilder(MessageColor.AQUA, MessageColor.AQUA, MessageColor.AQUA, MessageColor.BLUE)
                .message(message)
                .prefix("Staff");
    }
}
