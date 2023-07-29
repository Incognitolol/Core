package rip.alpha.core.shared.punishments;

import lombok.AllArgsConstructor;
import lombok.Getter;
import rip.alpha.core.shared.grants.Grant;

import java.util.Objects;

public record Punishment(Type type, String sender, String reason, long start, long duration) {
    public Punishment(Type type, String sender, String reason, long duration) {
        this(type, sender, reason, System.currentTimeMillis(), duration);
    }

    public boolean isInfinite() {
        return this.type == Type.SERVER_WARN || this.duration <= -1;
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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Punishment punishment)) {
            return false;
        }

        return punishment.type().equals(this.type) &&
                punishment.reason().equals(this.reason) &&
                punishment.duration() == this.duration &&
                punishment.sender().equals(this.sender) &&
                punishment.start() == this.start;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.type.ordinal(), this.sender, this.reason, this.start, this.duration);
    }

    @Getter
    @AllArgsConstructor
    public enum Type {
        SERVER_BAN("Ban", "Banned", "You have been {} banned", "Your ban has expired."),
        SERVER_MUTE("Mute", "Muted", "You have been {} muted", "Your mute has expired."),
        SERVER_WARN("Warn", "Warned", "You have been warned for {}.", "Your warning has expired."),
        BLACKLIST("Blacklist", "Blacklisted", "You have been {} blacklisted.", "Your blacklist has expired.");

        private final String typeName, participle, punishedMessage, expiredMessage;

    }
}
