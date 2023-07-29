package rip.alpha.core.bukkit.totp;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TotpAuthenticatedCache {

    @Getter
    private static final TotpAuthenticatedCache instance = new TotpAuthenticatedCache();

    private final Set<UUID> authenticatedIds = new HashSet<>();

    public boolean exists(Player player) {
        return this.exists(player.getUniqueId());
    }

    public boolean exists(UUID uuid) {
        return this.authenticatedIds.contains(uuid);
    }

    public void add(UUID uuid) {
        this.authenticatedIds.add(uuid);
    }

    public void remove(UUID uuid) {
        this.authenticatedIds.remove(uuid);
    }

    public static void handleTotpResetBridge(TotpResetBridgeEvent event) {
        instance.remove(event.uuid());
    }
}
