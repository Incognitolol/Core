package rip.alpha.core.bukkit.server.context;

import rip.alpha.core.shared.server.NetworkServer;
import rip.alpha.core.shared.server.NetworkServerSnapshotHandler;
import rip.alpha.core.shared.server.NetworkServerStatus;
import rip.alpha.libraries.command.context.ContextResolver;
import rip.alpha.libraries.command.context.type.ArgumentContext;
import rip.alpha.libraries.command.context.type.TabCompleteArgumentContext;
import rip.alpha.libraries.util.message.MessageBuilder;

import java.util.ArrayList;
import java.util.List;

public class NetworkServerSnapshotContextResolver implements ContextResolver<NetworkServer.NetworkServerSnapshot> {
    @Override
    public NetworkServer.NetworkServerSnapshot resolve(ArgumentContext<NetworkServer.NetworkServerSnapshot> argumentContext) {
        NetworkServer.NetworkServerSnapshot snapshot = NetworkServerSnapshotHandler.getInstance().getCachedServer(argumentContext.input().toLowerCase());

        if (snapshot == null || snapshot.getNetworkServerStatus() == NetworkServerStatus.OFFLINE) {
            argumentContext.sender().sendMessage(MessageBuilder.construct("The server '{}' does not exist", argumentContext.input()));
            return null;
        }

        return snapshot;
    }

    @Override
    public List<String> getTabComplete(TabCompleteArgumentContext<NetworkServer.NetworkServerSnapshot> tabCompleteArgumentContext) {
        List<String> tabComplete = new ArrayList<>();
        NetworkServerSnapshotHandler.getInstance().getCachedServers().forEach(snapshot -> {
            if (snapshot.getNetworkServerStatus() != NetworkServerStatus.OFFLINE){
                tabComplete.add(snapshot.getServerId());
            }
        });
        return tabComplete;
    }
}
