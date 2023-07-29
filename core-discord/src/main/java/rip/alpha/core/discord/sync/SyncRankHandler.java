package rip.alpha.core.discord.sync;

import rip.alpha.core.discord.sync.listener.ProfileSyncListener;
import rip.alpha.core.shared.AlphaCore;
import rip.alpha.core.shared.data.events.ProfileSyncEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Moose1301
 * @date 4/10/2022
 */
public class SyncRankHandler {
    private Map<String, UUID> syncCodes = new HashMap<>();

    public SyncRankHandler() {
        AlphaCore.registerListener(ProfileSyncEvent.class, ProfileSyncListener::onProfileSyncCreate);
    }

    public void addSyncCode(String code, UUID profileId) {
        this.syncCodes.put(code, profileId);
    }
    public UUID getProfile(String code) {
        return syncCodes.getOrDefault(code, null);
    }
    public void removeSyncCode(String code) {
        syncCodes.remove(code);
    }
}
