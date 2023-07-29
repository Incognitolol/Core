package rip.alpha.core.shared.queue;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.*;

public class NetworkQueue {

    @Getter
    private final String serverId;
    private final Queue<NetworkQueuePlayer> players;

    public NetworkQueue(String serverId) {
        this.serverId = serverId;
        this.players = new PriorityQueue<>();
    }

    public List<NetworkQueuePlayer> getPlayers() {
        return new ArrayList<>(this.players);
    }

    public NetworkQueuePlayer peek() {
        return this.players.peek();
    }

    public NetworkQueuePlayer poll() {
        NetworkQueuePlayer player = this.players.poll();
        this.updateQueue();
        return player;
    }

    public Optional<NetworkQueuePlayer> getPlayer(UUID playerId) {
        return this.players.stream().filter(networkQueuePlayer -> networkQueuePlayer.playerId().equals(playerId)).findFirst();
    }

    public int getPosition(UUID playerId) {
        NetworkQueuePlayer player = this.getPlayer(playerId).orElse(null);

        if (player == null) {
            return -1;
        }

        return new ArrayList<>(this.players).indexOf(player);
    }

    public int addToQueue(UUID playerId, int priority) {
        NetworkQueuePlayer player = new NetworkQueuePlayer(playerId, priority, System.currentTimeMillis());
        boolean accepted = !this.players.contains(player) && this.players.offer(player);

        if (accepted) {
            this.updateQueue();
        }

        return new ArrayList<>(this.players).indexOf(player);
    }

    public void removeFromQueue(UUID uuid) {
        if (this.players.isEmpty()) {
            return;
        }

        boolean valid = this.players.removeIf(networkQueuePlayer -> uuid.equals(networkQueuePlayer.playerId));

        if (valid) {
            this.updateQueue();
        }
    }

    public boolean inQueue(UUID uuid) {
        for (NetworkQueuePlayer queuedPlayer : this.players) {
            if (queuedPlayer.playerId.equals(uuid)) {
                return true;
            }
        }
        return false;
    }

    public boolean isEmpty() {
        return this.players.isEmpty();
    }

    public NetworkQueueSnapShot snapshot() {
        return new NetworkQueueSnapShot(this.serverId, this.players);
    }

    private void updateQueue() {
        NetworkQueueHandler.getInstance().putQueueInMap(this);
    }

    @RequiredArgsConstructor
    public static class NetworkQueueSnapShot {

        @Getter
        private final String serverId;
        private final Queue<NetworkQueuePlayer> players;

        public Optional<NetworkQueuePlayer> getPlayer(UUID playerId) {
            return this.players.stream().filter(networkQueuePlayer -> networkQueuePlayer.playerId().equals(playerId)).findFirst();
        }

        public int getPosition(UUID playerId) {
            NetworkQueuePlayer player = this.getPlayer(playerId).orElse(null);

            if (player == null) {
                return -1;
            }

            return new ArrayList<>(this.players).indexOf(player);
        }

        public boolean inQueue(UUID uuid) {
            for (NetworkQueuePlayer queuedPlayer : this.players) {
                if (queuedPlayer.playerId.equals(uuid)) {
                    return true;
                }
            }
            return false;
        }

        public List<NetworkQueuePlayer> getPlayers() {
            return new ArrayList<>(this.players);
        }

        public boolean isEmpty() {
            return this.players.isEmpty();
        }
    }

    public record NetworkQueuePlayer(UUID playerId, int priority,
                                     long joinTime) implements Comparable<NetworkQueuePlayer>, Serializable {

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof NetworkQueuePlayer player)) {
                return false;
            }
            return player.playerId().equals(this.playerId());
        }

        @Override
        public int compareTo(NetworkQueuePlayer other) {
            int comparedPriority = Integer.compare(this.priority, other.priority);

            if (comparedPriority == 0) {
                return Long.compare(this.joinTime, other.joinTime);
            }

            return comparedPriority;
        }
    }
}
