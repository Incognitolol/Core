package rip.alpha.core.shared.server;

import lombok.*;
import rip.alpha.core.shared.AlphaCoreConfig;
import rip.alpha.core.shared.server.event.NetworkServerRECommandEvent;
import rip.alpha.core.shared.server.event.NetworkServerREShutdownEvent;
import rip.alpha.libraries.json.GsonProvider;

import java.lang.reflect.Type;
import java.util.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NetworkServer {

    private static final long MAX_LIFETIME_DELTA = 3000L;

    @Getter
    private String serverId;

    @Getter
    private String displayName;

    @Getter
    private NetworkServerType serverType;

    @Getter
    private NetworkServerPlatform serverPlatform;

    @Setter
    private Set<NetworkServerEntity> connectedEntities;

    @Setter
    private NetworkServerStatus networkServerStatus;

    @Setter(AccessLevel.PROTECTED)
    private long lastHeartBeat;

    private Map<String, String> metadata;

    protected NetworkServer(AlphaCoreConfig.ServerInformation serverInformation) {
        this.serverId = serverInformation.getId();
        this.displayName = serverInformation.getDisplayName();
        this.serverType = serverInformation.getServerType();
        this.serverPlatform = serverInformation.getPlatform();
        this.connectedEntities = new HashSet<>();
        this.networkServerStatus = NetworkServerStatus.ONLINE;
        this.metadata = new HashMap<>();
        this.lastHeartBeat = System.currentTimeMillis();
    }

    public Collection<NetworkServerEntity> getConnectedEntities() {
        return Collections.unmodifiableCollection(this.connectedEntities);
    }

    protected boolean isDead() {
        return System.currentTimeMillis() - this.lastHeartBeat > MAX_LIFETIME_DELTA;
    }

    public NetworkServerStatus getNetworkServerStatus() {
        if (this.isDead()) {
            return NetworkServerStatus.OFFLINE;
        }

        return this.networkServerStatus;
    }

    public void shutdown(int duration) {
        new NetworkServerREShutdownEvent(this.serverId, duration).callEvent();
    }

    public void executeCommand(String command) {
        new NetworkServerRECommandEvent(this.serverId, command).callEvent();
    }

    public boolean hasMetadataValue(String key) {
        return this.metadata.containsKey(key);
    }

    public void removeMetadataValue(String key) {
        this.metadata.remove(key);
    }

    public void setMetadataValue(String key, Object value) {
        String json = GsonProvider.toJson(value);

        if (json == null) {
            return;
        }

        this.metadata.put(key, json);
    }

    public <T> T getMetadataValue(String key, Class<T> clazz) {
        String json = this.metadata.get(key);

        if (json == null) {
            return null;
        }

        return GsonProvider.fromJson(json, clazz);
    }

    public <T> T getMetadataValue(String key, Type type) {
        String json = this.metadata.get(key);

        if (json == null) {
            return null;
        }

        return GsonProvider.fromJson(json, type);
    }

    public NetworkServerSnapshot snapshot() {
        return new NetworkServerSnapshot(this.serverId, this.displayName, this.serverType,
                this.serverPlatform, this.connectedEntities, this.getNetworkServerStatus(), this.metadata, this.lastHeartBeat);
    }


    @RequiredArgsConstructor
    public static class NetworkServerSnapshot {
        @Getter
        private final String serverId;

        @Getter
        private final String displayName;

        @Getter
        private final NetworkServerType serverType;

        @Getter
        private final NetworkServerPlatform serverPlatform;

        @Getter
        private final Set<NetworkServerEntity> connectedEntities;
        private final NetworkServerStatus networkServerStatus;
        private final Map<String, String> metadata;
        private final long lastHeartBeat;

        protected boolean isDead() {
            return System.currentTimeMillis() - this.lastHeartBeat > MAX_LIFETIME_DELTA;
        }

        public NetworkServerStatus getNetworkServerStatus() {
            if (this.isDead()) {
                return NetworkServerStatus.OFFLINE;
            }

            return this.networkServerStatus;
        }

        public boolean hasMetadataValue(String key) {
            return this.metadata.containsKey(key);
        }

        public <T> T getMetadataValue(String key, Class<T> clazz) {
            String json = this.metadata.get(key);

            if (json == null) {
                return null;
            }

            return GsonProvider.fromJson(json, clazz);
        }

        public <T> T getMetadataValue(String key, Type type) {
            String json = this.metadata.get(key);

            if (json == null) {
                return null;
            }

            return GsonProvider.fromJson(json, type);
        }
    }
}
