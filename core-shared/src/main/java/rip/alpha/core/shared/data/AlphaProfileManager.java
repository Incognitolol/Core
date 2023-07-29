package rip.alpha.core.shared.data;

import rip.alpha.core.shared.AlphaCore;
import rip.alpha.libraries.model.DataManager;
import rip.alpha.libraries.model.DomainContext;
import rip.alpha.libraries.model.GlobalDataDomain;

import java.util.UUID;

public class AlphaProfileManager {

    private static final AlphaProfileManager instance = new AlphaProfileManager();

    public static GlobalDataDomain<UUID, AlphaProfile> profiles() {
        return instance.profileDomain;
    }

    private final GlobalDataDomain<UUID, AlphaProfile> profileDomain;

    private AlphaProfileManager() {
        DomainContext<UUID, AlphaProfile> context = DomainContext.<UUID, AlphaProfile>builder()
                .keyFunction(AlphaProfile::getMojangID)
                .redissonClient(AlphaCore.getRedissonClient())
                .keyClass(UUID.class)
                .valueClass(AlphaProfile.class)
                .creator(AlphaProfile::new)
                .mongoDatabase(AlphaCore.getDatabase())
                .namespace("alpha-profiles")
                .build();
        this.profileDomain = DataManager.getInstance().getOrCreateGlobalDomain(context);
    }

}
