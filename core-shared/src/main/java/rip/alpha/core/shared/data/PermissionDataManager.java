package rip.alpha.core.shared.data;

import rip.alpha.core.shared.AlphaCore;
import rip.alpha.core.shared.ranks.Rank;
import rip.alpha.libraries.model.DataManager;
import rip.alpha.libraries.model.DomainContext;
import rip.alpha.libraries.model.GlobalDataDomain;

public class PermissionDataManager {

    private static final PermissionDataManager instance = new PermissionDataManager();

    public static GlobalDataDomain<Rank, PermissionData> permissions() {
        return instance.profileDomain;
    }

    private final GlobalDataDomain<Rank, PermissionData> profileDomain;

    private PermissionDataManager() {
        DomainContext<Rank, PermissionData> context = DomainContext.<Rank, PermissionData>builder()
                .keyFunction(PermissionData::getRank)
                .redissonClient(AlphaCore.getRedissonClient())
                .keyClass(Rank.class)
                .valueClass(PermissionData.class)
                .creator(PermissionData::new)
                .mongoDatabase(AlphaCore.getDatabase())
                .namespace("rank-permissions")
                .build();
        this.profileDomain = DataManager.getInstance().getOrCreateGlobalDomain(context);
        for (Rank rank : Rank.values()) {
            this.profileDomain.enableLocalCacheFor(rank);
        }
    }

}
