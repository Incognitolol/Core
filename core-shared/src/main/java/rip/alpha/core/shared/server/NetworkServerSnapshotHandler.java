package rip.alpha.core.shared.server;

import lombok.Getter;
import rip.alpha.core.shared.server.event.NetworkServerUpdateEvent;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NetworkServerSnapshotHandler {
    @Getter
    private static final NetworkServerSnapshotHandler instance = new NetworkServerSnapshotHandler();
    private final Map<String, NetworkServer.NetworkServerSnapshot> snapshotMap;

    public NetworkServerSnapshotHandler() {
        this.snapshotMap = new ConcurrentHashMap<>();
    }

    protected void cacheServer(NetworkServer networkServer) {
        this.snapshotMap.put(networkServer.getServerId(), networkServer.snapshot());
    }

    public NetworkServer.NetworkServerSnapshot getCachedServer(String serverId) {
        return this.snapshotMap.get(serverId);
    }

    public Collection<NetworkServer.NetworkServerSnapshot> getCachedServers() {
        return Collections.unmodifiableCollection(this.snapshotMap.values());
    }

    public static void handleNetworkServerUpdate(NetworkServerUpdateEvent event) {
        NetworkServerHandler.getInstance()
                .getServerAsync(event.serverId())
                .thenAccept(networkServer -> NetworkServerSnapshotHandler.getInstance().cacheServer(networkServer));
    }
}
