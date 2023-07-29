package rip.alpha.core.bukkit.data.server;

import rip.alpha.core.shared.AlphaCore;
import rip.alpha.core.shared.server.NetworkServerHandler;
import rip.alpha.libraries.model.DataManager;
import rip.alpha.libraries.model.DomainContext;
import rip.alpha.libraries.model.LocalDataDomain;

public class LocalServerDataManager {

    private static final String SERVER_DATA = "local-server-data";
    private static final String CURRENT_SERVER = NetworkServerHandler.getInstance().getCurrentServer().getServerId();

    private static final LocalServerDataManager instance = new LocalServerDataManager();

    public static LocalServerData getCurrent() {
        return instance.dataDomain.getData(CURRENT_SERVER);
    }

    public static void loadCurrent() {
        instance.dataDomain.loadDataSync(CURRENT_SERVER);
    }

    public static void unLoadCurrent() {
        instance.dataDomain.unloadDataSync(CURRENT_SERVER);
    }

    private final LocalDataDomain<String, LocalServerData> dataDomain;

    private LocalServerDataManager() {
        DomainContext<String, LocalServerData> context = DomainContext.<String, LocalServerData>builder()
                .keyFunction(LocalServerData::getServerName)
                .redissonClient(AlphaCore.getRedissonClient())
                .keyClass(String.class)
                .valueClass(LocalServerData.class)
                .creator(LocalServerData::new)
                .mongoDatabase(AlphaCore.getDatabase())
                .namespace(SERVER_DATA)
                .build();
        this.dataDomain = DataManager.getInstance().getOrCreateLocalDomain(context);
    }

}
