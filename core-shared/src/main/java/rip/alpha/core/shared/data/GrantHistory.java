package rip.alpha.core.shared.data;

import rip.alpha.core.shared.grants.Grant;
import rip.alpha.core.shared.ranks.Rank;

import java.util.ArrayList;
import java.util.List;

public class GrantHistory {

    private final List<Entry> history = new ArrayList<>();

    protected void add(Grant grant, String removedBy, String removedReason) {
        this.history.add(new Entry(grant.snapshot(), System.currentTimeMillis(), removedBy, removedReason));
    }

    public List<Entry> getEntries() {
        return List.copyOf(this.history);
    }

    public record Entry(Grant.RankNameSnapshot grant, long timestamp, String removedBy, String removedReason) {

    }
}
