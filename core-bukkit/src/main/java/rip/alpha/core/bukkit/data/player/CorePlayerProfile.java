package rip.alpha.core.bukkit.data.player;

import lombok.Getter;
import lombok.Setter;
import rip.alpha.core.bukkit.essentials.backtrack.LocationTrack;
import rip.alpha.core.bukkit.levels.LevelData;

import java.util.UUID;

@Getter
public class CorePlayerProfile {

    private final UUID playerID;
    private final LevelData levelData;
    private final LocationTrack locationTrack;

    @Setter
    private long lastChatMessageTime = 0L;

    @Setter
    private UUID lastMessagedID = null;

    @Setter
    private String lastMessagedName = null;

    protected CorePlayerProfile() {
        this(null);
    }

    protected CorePlayerProfile(UUID playerID) {
        this.playerID = playerID;
        this.levelData = new LevelData();
        this.locationTrack = new LocationTrack();
    }

}
