package rip.alpha.core.bukkit.server;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerWhitelistEvent;
import org.bukkit.event.server.ServerMaxPlayerChangeEvent;
import org.bukkit.event.server.ServerWhitelistEvent;
import rip.alpha.core.shared.reboot.RebootHandler;
import rip.alpha.core.shared.server.NetworkServer;
import rip.alpha.core.shared.server.NetworkServerEntity;
import rip.alpha.core.shared.server.NetworkServerHandler;
import rip.alpha.core.shared.server.NetworkServerStatus;
import rip.alpha.core.shared.server.event.*;
import rip.alpha.libraries.util.message.MessageBuilder;
import rip.alpha.libraries.util.task.TaskUtil;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class NetworkServerListener implements Listener {

    public NetworkServerListener() {
        this.updateServerIpAndPort();
        this.updateOnlinePlayers();
        this.updateServerStatus();
        this.updateWhitelistedPlayers();
        this.updateMaxPlayers();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        this.updateOnlinePlayers();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        TaskUtil.runTaskLater(this::updateOnlinePlayers, 20);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onServerWhitelist(ServerWhitelistEvent event) {
        this.updateServerStatus();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerWhitelist(PlayerWhitelistEvent event) {
        this.updateWhitelistedPlayers();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onServerMaxPlayerChange(ServerMaxPlayerChangeEvent event) {
        this.updateMaxPlayers();
    }

    public static void handleAddServer(NetworkServerAddEvent event) {
        String message = MessageBuilder.standard("Adding server {}...")
                .element(event.serverId())
                .prefix("Monitor")
                .build();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.isOp()) { //TODO add a permission
                player.sendMessage(message);
            }
        }
    }

    public static void handleRemoveServer(NetworkServerRemoveEvent event) {
        String message = MessageBuilder.standard("Removing server {}...")
                .element(event.serverId())
                .prefix("Monitor")
                .build();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.isOp()) { //TODO add a permission
                player.sendMessage(message);
            }
        }
    }

    public static void handleREBroadcast(NetworkServerREBroadcastEvent event) {
        String targetServer = event.targetServer();
        if (!targetServer.equalsIgnoreCase("*") && !targetServer.equalsIgnoreCase("bukkit")) {
            if (!event.targetServer().equalsIgnoreCase(NetworkServerHandler.getInstance().getCurrentServer().getServerId())) {
                return;
            }
        }

        if (event.permission() != null && !event.permission().isEmpty()) {
            Bukkit.broadcast(event.translatedMessage(), event.permission());
            return;
        }

        Bukkit.broadcastMessage(event.translatedMessage());
    }

    public static void handleREShutdown(NetworkServerREShutdownEvent event) {
        String targetServer = event.targetServer();
        if (!targetServer.equalsIgnoreCase("*") && !targetServer.equalsIgnoreCase("bukkit")) {
            if (!event.targetServer().equalsIgnoreCase(NetworkServerHandler.getInstance().getCurrentServer().getServerId())) {
                return;
            }
        }

        RebootHandler.getInstance().startReboot(event.duration());
    }

    public static void handleRECommand(NetworkServerRECommandEvent event) {
        String targetServer = event.targetServer();
        if (!targetServer.equalsIgnoreCase("*") && !targetServer.equalsIgnoreCase("bukkit")) {
            if (!event.targetServer().equalsIgnoreCase(NetworkServerHandler.getInstance().getCurrentServer().getServerId())) {
                return;
            }
        }
        TaskUtil.runSync(() -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), event.command()));
    }

    private void updateServerIpAndPort() {
        NetworkServer server = NetworkServerHandler.getInstance().getCurrentServer();
        Server bukkitServer = Bukkit.getServer();
        String ip = bukkitServer.getIp().length() > 0 ? bukkitServer.getIp() : "localhost";
        int port = bukkitServer.getPort();
        server.setMetadataValue("serverIp", ip);
        server.setMetadataValue("serverPort", port);
        NetworkServerHandler.getInstance().updateCurrentServerAsync();
    }

    private void updateOnlinePlayers() {
        NetworkServer server = NetworkServerHandler.getInstance().getCurrentServer();
        server.setConnectedEntities(this.getOnlineEntities());
        NetworkServerHandler.getInstance().updateCurrentServerAsync();
    }

    private void updateServerStatus() {
        NetworkServer server = NetworkServerHandler.getInstance().getCurrentServer();
        server.setNetworkServerStatus(Bukkit.hasWhitelist() ? NetworkServerStatus.WHITELISTED : NetworkServerStatus.ONLINE);
        NetworkServerHandler.getInstance().updateCurrentServerAsync();
    }

    private void updateWhitelistedPlayers() {
        NetworkServer server = NetworkServerHandler.getInstance().getCurrentServer();
        server.setMetadataValue("whitelistedPlayers", this.getWhitelistedPlayers());
        NetworkServerHandler.getInstance().updateCurrentServerAsync();
    }

    private void updateMaxPlayers() {
        NetworkServer server = NetworkServerHandler.getInstance().getCurrentServer();
        server.setMetadataValue("maxPlayers", Bukkit.getMaxPlayers());
        NetworkServerHandler.getInstance().updateCurrentServerAsync();
    }

    private Set<NetworkServerEntity> getOnlineEntities() {
        return Bukkit.getOnlinePlayers()
                .stream()
                .map(player -> new NetworkServerEntity(player.getUniqueId(), player.getName()))
                .collect(Collectors.toSet());
    }

    private Set<UUID> getWhitelistedPlayers() {
        Set<UUID> whitelistedPlayers = Bukkit.getWhitelistedPlayers().stream().map(OfflinePlayer::getUniqueId).collect(Collectors.toSet());
        Bukkit.getOperators().forEach(offlinePlayer -> whitelistedPlayers.add(offlinePlayer.getUniqueId()));
        return whitelistedPlayers;
    }
}
