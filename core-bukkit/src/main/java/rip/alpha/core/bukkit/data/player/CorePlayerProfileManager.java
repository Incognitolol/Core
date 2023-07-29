package rip.alpha.core.bukkit.data.player;

import rip.alpha.core.shared.AlphaCore;
import rip.alpha.libraries.model.DataManager;
import rip.alpha.libraries.model.DomainContext;
import rip.alpha.libraries.model.LocalDataDomain;

import java.util.UUID;

public class CorePlayerProfileManager {

    private static final CorePlayerProfileManager instance = new CorePlayerProfileManager();

    public static LocalDataDomain<UUID, CorePlayerProfile> profiles() {
        return instance.profileDomain;
    }

    private final LocalDataDomain<UUID, CorePlayerProfile> profileDomain;

    private CorePlayerProfileManager() {
        DomainContext<UUID, CorePlayerProfile> context = DomainContext.<UUID, CorePlayerProfile>builder()
                .keyFunction(CorePlayerProfile::getPlayerID)
                .redissonClient(AlphaCore.getRedissonClient())
                .keyClass(UUID.class)
                .valueClass(CorePlayerProfile.class)
                .creator(CorePlayerProfile::new)
                .mongoDatabase(AlphaCore.getDatabase())
                .namespace("core-profiles")
                .build();
        this.profileDomain = DataManager.getInstance().getOrCreateLocalDomain(context);
    }
}
