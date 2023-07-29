package rip.alpha.core.bukkit.modsuite.modmode;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ModModeEntryCache {

    @Getter
    private static final ModModeEntryCache instance = new ModModeEntryCache();

    private final Map<UUID, ModModeEntry> modModeEntryMap = new HashMap<>();

    public boolean exists(Player player) {
        return this.exists(player.getUniqueId());
    }

    public boolean exists(UUID uuid) {
        return this.modModeEntryMap.containsKey(uuid);
    }

    public void add(UUID uuid, ModModeEntry entry) {
        this.modModeEntryMap.put(uuid, entry);
    }

    public void remove(UUID uuid) {
        this.modModeEntryMap.remove(uuid);
    }

    public ModModeEntry get(UUID uuid) {
        return this.modModeEntryMap.get(uuid);
    }

    public ModModeEntry computeIfAbsent(Player player) {
        return this.computeIfAbsent(player.getUniqueId());
    }

    public ModModeEntry computeIfAbsent(UUID uuid) {
        return this.modModeEntryMap.computeIfAbsent(uuid, id -> new ModModeEntry());
    }
}
