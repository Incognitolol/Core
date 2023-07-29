package rip.alpha.core.shared.grants;

import rip.alpha.core.shared.ranks.Rank;

import java.util.Objects;

public record Grant(Rank rank, String sender, String reason, long start, long duration) {
    public Grant(Rank rank, String sender, String reason, long duration) {
        this(rank, sender, reason, System.currentTimeMillis(), duration);
    }

    public boolean isInfinite() {
        return this.duration <= -1;
    }

    public long getRunOutTimestamp() {
        return this.isInfinite() ? Long.MAX_VALUE : this.start + this.duration;
    }

    public long getTimeLeft() {
        return this.isInfinite() ? Long.MAX_VALUE : this.getRunOutTimestamp() - System.currentTimeMillis();
    }

    public boolean hasExpired() {
        return this.getTimeLeft() <= 0;
    }

    public RankNameSnapshot snapshot() {
        return new RankNameSnapshot(this.rank.name(), this.sender, this.reason, this.start, this.duration);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Grant grant)) {
            return false;
        }

        return grant.rank().equals(this.rank) &&
                grant.reason().equals(this.reason) &&
                grant.duration() == this.duration &&
                grant.sender().equals(this.sender) &&
                grant.start() == this.start;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.rank.ordinal(), this.sender, this.reason, this.start, this.duration);
    }

    public record RankNameSnapshot(String rank, String sender, String reason, long start, long duration) {
        public boolean isInfinite() {
            return this.duration <= -1;
        }

        public Rank toRankEnum() {
            try {
                return Rank.valueOf(this.rank);
            } catch (IllegalArgumentException e){
                return null;
            }
        }

        public long getRunOutTimestamp() {
            return this.isInfinite() ? Long.MAX_VALUE : this.start + this.duration;
        }
    }
}
