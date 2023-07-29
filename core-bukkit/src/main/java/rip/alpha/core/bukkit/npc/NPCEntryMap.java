package rip.alpha.core.bukkit.npc;

import java.util.*;

public class NPCEntryMap {

    private final Map<Integer, NPCEntry> entryMap = new HashMap<>();

    public Collection<NPCEntry> getAllEntries() {
        return Collections.unmodifiableCollection(entryMap.values());
    }

    public void putAllEntries(List<NPCEntry> entries) {
        entries.forEach(this::putEntry);
    }

    private void putEntry(NPCEntry entry) {
        this.entryMap.put(entry.getId(), entry);
    }
}
