package rip.alpha.core.velocity.server;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.proxy.server.ServerInfo;
import net.kyori.adventure.text.Component;
import rip.alpha.core.shared.reboot.RebootHandler;
import rip.alpha.core.shared.server.NetworkServer;
import rip.alpha.core.shared.server.NetworkServerEntity;
import rip.alpha.core.shared.server.NetworkServerHandler;
import rip.alpha.core.shared.server.NetworkServerPlatform;
import rip.alpha.core.shared.server.event.*;
import rip.alpha.core.velocity.Core;

import java.net.InetSocketAddress;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class NetworkServerListener {
    public NetworkServerListener() {
        this.registerServers();
        this.updatePlayerCount();
    }

    @Subscribe(order = PostOrder.LAST)
    public void onPlayerConnect(PostLoginEvent event) {
        this.updatePlayerCount();
    }

    @Subscribe(order = PostOrder.LAST)
    public void onDisconnect(DisconnectEvent event) {
        this.updatePlayerCount();
    }

    private void updatePlayerCount() {
        NetworkServer server = NetworkServerHandler.getInstance().getCurrentServer();
        server.setConnectedEntities(this.getOnlineEntities());
        NetworkServerHandler.getInstance().updateCurrentServerAsync();
    }

    private Set<NetworkServerEntity> getOnlineEntities() {
        return Core.getInstance().getProxyServer().getAllPlayers()
                .stream()
                .map(player -> new NetworkServerEntity(player.getUniqueId(), player.getUsername()))
                .collect(Collectors.toSet());
    }

    private void registerServers() {
        NetworkServerHandler.getInstance().getServersAsync().thenAccept(networkServers -> {
            for (NetworkServer networkServer : networkServers) {
                ServerInfo info = getServerInfo(networkServer);

                if (info == null) {
                    continue;
                }

                Core.getInstance().getProxyServer().registerServer(info);
            }
        });
    }

    public static void handleREBroadcast(NetworkServerREBroadcastEvent event) {
        String targetServer = event.targetServer();
        if (!targetServer.equalsIgnoreCase("*") && !targetServer.equalsIgnoreCase("velocity")) {
            if (!event.targetServer().equalsIgnoreCase(NetworkServerHandler.getInstance().getCurrentServer().getServerId())) {
                return;
            }
        }

        if (event.permission() != null && !event.permission().isEmpty()) {
            Core.getInstance().getProxyServer().getAllPlayers().forEach(player -> {
                if (player.hasPermission(event.permission())) {
                    player.sendMessage(Component.text(event.translatedMessage()));
                }
            });
            return;
        }

        Core.getInstance().getProxyServer().getAllPlayers().forEach(player -> player.sendMessage(Component.text(event.translatedMessage())));
    }

    public static void handleREShutdown(NetworkServerREShutdownEvent event) {
        String targetServer = event.targetServer();
        if (!targetServer.equalsIgnoreCase("*") && !targetServer.equalsIgnoreCase("velocity")) {
            if (!event.targetServer().equalsIgnoreCase(NetworkServerHandler.getInstance().getCurrentServer().getServerId())) {
                return;
            }
        }
        RebootHandler.getInstance().startReboot(event.duration());
    }

    public static void handleRECommand(NetworkServerRECommandEvent event) {
        String targetServer = event.targetServer();
        if (!targetServer.equalsIgnoreCase("*") && !targetServer.equalsIgnoreCase("velocity")) {
            if (!event.targetServer().equalsIgnoreCase(NetworkServerHandler.getInstance().getCurrentServer().getServerId())) {
                return;
            }
        }
        Core.getInstance().getProxyServer().getCommandManager().executeAsync(new NetworkServerREExecutor(), event.command());
    }

    public static void handleAddServer(NetworkServerAddEvent event) {
        getServerInfoFromId(event.serverId(), serverInfo -> {
            if (serverInfo == null) {
                return;
            }
            Core.getInstance().getProxyServer().registerServer(serverInfo);
        });
    }

    public static void handleSendPlayer(NetworkServerSendPlayerEvent event){
        Core.getInstance().getProxyServer().getPlayer(event.playerId()).ifPresent(player ->
                Core.getInstance().getProxyServer().getServer(event.serverTo()).ifPresent(registeredServer ->
                        player.createConnectionRequest(registeredServer).fireAndForget()));
    }

    public static void handleSendServer(NetworkServerSendServerEvent event){
        Core.getInstance().getProxyServer().getServer(event.serverFrom()).ifPresent(fromServer ->
                Core.getInstance().getProxyServer().getServer(event.serverTo()).ifPresent(toServer ->
                        fromServer.getPlayersConnected().forEach(player -> player.createConnectionRequest(toServer).fireAndForget())));
    }

    public static void handleSendAll(NetworkServerSendAllEvent event){
        Core.getInstance().getProxyServer().getServer(event.toServer()).ifPresent(registeredServer ->
                Core.getInstance().getProxyServer().getAllPlayers().forEach(player ->
                        player.createConnectionRequest(registeredServer).fireAndForget()));
    }

    private static void getServerInfoFromId(String serverId, Consumer<ServerInfo> consumer) {
        NetworkServerHandler.getInstance().getServerAsync(serverId).thenAccept(server -> consumer.accept(getServerInfo(server)));
    }

    private static ServerInfo getServerInfo(NetworkServer networkServer) {
        if (networkServer == null || networkServer.getServerPlatform() != NetworkServerPlatform.BUKKIT) {
            return null;
        }

        String serverIp = networkServer.getMetadataValue("serverIp", String.class);
        int serverPort = networkServer.getMetadataValue("serverPort", int.class);
        return new ServerInfo(networkServer.getServerId(), new InetSocketAddress(serverIp, serverPort));
    }
}
