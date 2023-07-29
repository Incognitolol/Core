package rip.alpha.core.shared.queue;

import lombok.Getter;
import org.redisson.api.RMap;
import rip.alpha.core.shared.AlphaCore;
import rip.alpha.core.shared.data.AlphaProfile;
import rip.alpha.core.shared.data.AlphaProfileManager;
import rip.alpha.core.shared.queue.event.NetworkQueueUpdateEvent;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class NetworkQueueHandler {

    @Getter
    private static final NetworkQueueHandler instance = new NetworkQueueHandler();

    private final RMap<String, NetworkQueue> queueMap;

    private NetworkQueueHandler() {
        this.queueMap = AlphaCore.getRedissonClient().getMap("networkQueue");
    }

    public CompletableFuture<NetworkQueue> getQueueAsync(String serverId) {
        return CompletableFuture.supplyAsync(() -> this.queueMap.computeIfAbsent(serverId.toLowerCase(), NetworkQueue::new));
    }

    public CompletableFuture<Collection<NetworkQueue>> getQueuesAsync() {
        return CompletableFuture.supplyAsync(() -> Collections.unmodifiableCollection(this.queueMap.readAllValues()));
    }

    public CompletableFuture<Void> leaveQueue(UUID playerId) {
        return this.getQueuesAsync().thenAccept(networkQueues -> networkQueues.forEach(networkQueue -> networkQueue.removeFromQueue(playerId)));
    }

    public CompletableFuture<Integer> joinQueue(UUID playerId, String serverId) {
        CompletableFuture<AlphaProfile> profileFuture = AlphaProfileManager.profiles().getOrCreateRealTimeDataAsync(playerId);
        return NetworkQueueHandler.getInstance()
                .getQueueAsync(serverId)
                .thenCombine(profileFuture, (queue, profile) -> queue.addToQueue(playerId, profile.getHighestRank().getPriority()) + 1);
    }

    public CompletableFuture<Boolean> inQueue(UUID playerId) {
        return this.getQueuesAsync().thenApply(networkQueues -> {
            for (NetworkQueue networkQueue : networkQueues) {
                if (networkQueue.inQueue(playerId)) {
                    return true;
                }
            }
            return false;
        });
    }

    protected void putQueueInMap(NetworkQueue networkQueue) {
        this.queueMap.put(networkQueue.getServerId(), networkQueue);
        new NetworkQueueUpdateEvent(networkQueue.getServerId()).callEvent();
    }
}
