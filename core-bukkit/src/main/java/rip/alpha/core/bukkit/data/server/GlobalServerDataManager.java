package rip.alpha.core.bukkit.data.server;

import rip.alpha.core.shared.AlphaCore;
import rip.alpha.core.shared.server.NetworkServerHandler;
import rip.alpha.libraries.model.DataManager;
import rip.alpha.libraries.model.DomainContext;
import rip.alpha.libraries.model.GlobalDataDomain;

import java.util.concurrent.CompletableFuture;

public class GlobalServerDataManager {

    private static final String SERVER_DATA = "global-server-data";
    private static final String CURRENT_SERVER = NetworkServerHandler.getInstance().getCurrentServer().getServerId();

    private static final GlobalServerDataManager instance = new GlobalServerDataManager();

    public static GlobalDataDomain<String, GlobalServerData> servers() {
        return instance.dataDomain;
    }

    public static GlobalServerData getCurrent() {
        return instance.dataDomain.getOrCreateRealTimeData(CURRENT_SERVER);
    }

    public static CompletableFuture<GlobalServerData> getCurrentAsync() {
        return instance.dataDomain.getOrCreateRealTimeDataAsync(CURRENT_SERVER);
    }

    private final GlobalDataDomain<String, GlobalServerData> dataDomain;

    private GlobalServerDataManager() {
        DomainContext<String, GlobalServerData> context = DomainContext.<String, GlobalServerData>builder()
                .keyFunction(GlobalServerData::getServerName)
                .redissonClient(AlphaCore.getRedissonClient())
                .keyClass(String.class)
                .valueClass(GlobalServerData.class)
                .creator(GlobalServerData::new)
                .mongoDatabase(AlphaCore.getDatabase())
                .namespace(SERVER_DATA)
                .build();
        this.dataDomain = DataManager.getInstance().getOrCreateGlobalDomain(context);
    }

}
