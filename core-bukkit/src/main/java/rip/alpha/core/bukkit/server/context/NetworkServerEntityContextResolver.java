package rip.alpha.core.bukkit.server.context;

import rip.alpha.core.shared.server.NetworkServer;
import rip.alpha.core.shared.server.NetworkServerEntity;
import rip.alpha.core.shared.server.NetworkServerSnapshotHandler;
import rip.alpha.core.shared.server.NetworkServerStatus;
import rip.alpha.libraries.command.context.ContextResolver;
import rip.alpha.libraries.command.context.type.ArgumentContext;
import rip.alpha.libraries.command.context.type.TabCompleteArgumentContext;
import rip.alpha.libraries.util.message.MessageBuilder;

import java.util.*;

public class NetworkServerEntityContextResolver implements ContextResolver<NetworkServerEntity> {
    @Override
    public NetworkServerEntity resolve(ArgumentContext<NetworkServerEntity> argumentContext) {
        Collection<NetworkServer.NetworkServerSnapshot> snapshots = NetworkServerSnapshotHandler.getInstance().getCachedServers();

        for (NetworkServer.NetworkServerSnapshot snapshot : snapshots) {
            if (snapshot.getNetworkServerStatus() == NetworkServerStatus.OFFLINE){
                continue;
            }
            for (NetworkServerEntity connectedEntity : snapshot.getConnectedEntities()) {
                if (connectedEntity.entityName().equalsIgnoreCase(argumentContext.input())){
                    return connectedEntity;
                }
            }
        }

        argumentContext.sender().sendMessage(MessageBuilder.constructError("{} is not currently online on the network", argumentContext.input()));
        return null;
    }

    @Override
    public List<String> getTabComplete(TabCompleteArgumentContext<NetworkServerEntity> tabCompleteArgumentContext) {
        Collection<NetworkServer.NetworkServerSnapshot> snapshots = NetworkServerSnapshotHandler.getInstance().getCachedServers();
        Set<String> tabComplete = new HashSet<>();
        for (NetworkServer.NetworkServerSnapshot snapshot : snapshots) {
            if (snapshot.getNetworkServerStatus() == NetworkServerStatus.OFFLINE){
                continue;
            }
            for (NetworkServerEntity connectedEntity : snapshot.getConnectedEntities()) {
                tabComplete.add(connectedEntity.entityName());
            }
        }
        return new ArrayList<>(tabComplete);
    }
}
