package rip.alpha.core.shared.server;

import lombok.Getter;
import org.redisson.api.RMap;
import rip.alpha.core.shared.AlphaCore;
import rip.alpha.core.shared.server.event.NetworkServerAddEvent;
import rip.alpha.core.shared.server.event.NetworkServerRemoveEvent;
import rip.alpha.core.shared.server.event.NetworkServerUpdateEvent;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class NetworkServerHandler {

    @Getter
    private static final NetworkServerHandler instance = new NetworkServerHandler();

    @Getter
    private final NetworkServer currentServer;
    private final RMap<String, NetworkServer> serverMap;

    private NetworkServerHandler() {
        this.serverMap = AlphaCore.getRedissonClient().getMap("networkServers");
        this.currentServer = new NetworkServer(AlphaCore.getConfig().getServerInformation());

        // Delay of a second to allow things to initialize
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(this::updateCurrentServer, 1L, 2L, TimeUnit.SECONDS);
        NetworkServerAddEvent event = new NetworkServerAddEvent(this.currentServer.getServerId());
        Executors.newSingleThreadScheduledExecutor().schedule(event::callEvent, 1500L, TimeUnit.MILLISECONDS);
    }

    private NetworkServer getServer(String serverId) {
        return this.serverMap.get(serverId.toLowerCase());
    }

    public CompletableFuture<NetworkServer> getServerAsync(String serverId) {
        return CompletableFuture.supplyAsync(() -> this.getServer(serverId));
    }

    private Collection<NetworkServer> getServers() {
        return Collections.unmodifiableCollection(this.serverMap.readAllValues());
    }

    public CompletableFuture<Collection<NetworkServer>> getServersAsync() {
        return CompletableFuture.supplyAsync(this::getServers);
    }

    private Map<String, NetworkServer> getServers(String... servers) {
        Map<String, NetworkServer> networkServerMap = new HashMap<>();
        for (String server : servers) {
            networkServerMap.put(server, this.getServer(server));
        }
        return networkServerMap;
    }

    public CompletableFuture<Map<String, NetworkServer>> getServersAsync(String... servers) {
        return CompletableFuture.supplyAsync(() -> this.getServers(servers));
    }

    private Set<NetworkServer> getServersByType(NetworkServerType type) {
        Set<NetworkServer> servers = new HashSet<>();

        for (NetworkServer server : this.getServers()) {
            if (server.isDead()) {
                continue;
            }

            if (server.getServerType().equals(type)) {
                servers.add(server);
            }
        }

        return servers;
    }

    public CompletableFuture<Set<NetworkServer>> getServersByTypeAsync(NetworkServerType type) {
        return CompletableFuture.supplyAsync(() -> this.getServersByType(type));
    }

    private void updateCurrentServer() {
        this.currentServer.setLastHeartBeat(System.currentTimeMillis());
        this.serverMap.fastPut(this.currentServer.getServerId().toLowerCase(), this.currentServer);
        new NetworkServerUpdateEvent(this.currentServer.getServerId()).callEvent();
    }

    @SuppressWarnings("UnusedReturnValue")
    public CompletableFuture<Void> updateCurrentServerAsync() {
        return CompletableFuture.runAsync(this::updateCurrentServer);
    }

    public void disable() {
        this.serverMap.fastRemove(this.currentServer.getServerId().toLowerCase());
        new NetworkServerRemoveEvent(AlphaCore.getConfig().getServerInformation().getId()).callEvent();
    }
}
