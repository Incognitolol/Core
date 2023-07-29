package rip.alpha.core.shared.queue;

import lombok.Getter;
import rip.alpha.core.shared.queue.event.NetworkQueueUpdateEvent;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NetworkQueueSnapshotHandler {

    @Getter
    private static final NetworkQueueSnapshotHandler instance = new NetworkQueueSnapshotHandler();
    private final Map<String, NetworkQueue.NetworkQueueSnapShot> snapshotMap;

    public NetworkQueueSnapshotHandler() {
        this.snapshotMap = new ConcurrentHashMap<>();
    }

    protected void cacheQueue(NetworkQueue networkQueue) {
        this.snapshotMap.put(networkQueue.getServerId(), networkQueue.snapshot());
    }

    public NetworkQueue.NetworkQueueSnapShot getCachedQueue(String serverId) {
        return this.snapshotMap.get(serverId);
    }

    public Collection<NetworkQueue.NetworkQueueSnapShot> getCachedQueues() {
        return Collections.unmodifiableCollection(this.snapshotMap.values());
    }

    public static void handleNetworkQueueUpdate(NetworkQueueUpdateEvent event) {
        NetworkQueueHandler.getInstance()
                .getQueueAsync(event.serverId())
                .thenAccept(networkQueue -> NetworkQueueSnapshotHandler.getInstance().cacheQueue(networkQueue));
    }
}
