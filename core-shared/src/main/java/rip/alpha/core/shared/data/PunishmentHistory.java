package rip.alpha.core.shared.data;

import rip.alpha.core.shared.punishments.Punishment;

import java.util.ArrayList;
import java.util.List;

public class PunishmentHistory {

    private final List<PunishmentHistory.Entry> history = new ArrayList<>();

    protected void add(Punishment punishment, String removedBy, String removedReason) {
        this.history.add(new PunishmentHistory.Entry(punishment, System.currentTimeMillis(), removedBy, removedReason));
    }

    public List<PunishmentHistory.Entry> getEntries() {
        return List.copyOf(this.history);
    }

    public record Entry(Punishment punishment, long timestamp, String removedBy, String removedReason) {

    }
}
