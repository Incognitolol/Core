package rip.alpha.core.buycraft.response;

import java.util.PriorityQueue;
import java.util.UUID;

public record PlayersDueResponse(int nextCheck, PriorityQueue<QueuedPlayer> priorityQueue) {
    public record QueuedPlayer(int id, String name, String uuid) implements Comparable<QueuedPlayer> {
        public UUID formatUUID() {
            return UUID.fromString(this.uuid
                    .replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"));
        }

        @Override
        public int compareTo(QueuedPlayer o) {
            return Integer.compare(-o.id(), -this.id());
        }
    }
}
