package rip.alpha.core.buycraft.response;

import java.util.PriorityQueue;
import java.util.UUID;

public record CommandsDueResponse(PriorityQueue<QueuedCommand> queuedCommands) {
    public record QueuedCommand(int id, String command, int payment, int packageId, Player player) implements Comparable<QueuedCommand> {
        @Override
        public int compareTo(QueuedCommand o) {
            return Integer.compare(-o.id(), -this.id());
        }
    }

    public record Player(int id, String name, String uuid){
        public UUID formatUUID() {
            return UUID.fromString(this.uuid
                    .replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"));
        }
    }
}
