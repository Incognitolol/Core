package rip.alpha.core.shared;

import lombok.Data;
import lombok.Getter;
import rip.alpha.core.shared.server.NetworkServerPlatform;
import rip.alpha.core.shared.server.NetworkServerType;
import rip.alpha.libraries.configuration.Configuration;

import java.io.File;

@Getter
public class AlphaCoreConfig implements Configuration {

    private final ServerInformation serverInformation = new ServerInformation();

    @Override
    public File getFileLocation() {
        return new File("core/", "config.json");
    }

    @Data
    public static class ServerInformation {
        public String id = "serverId";
        public String displayName = "serverName";
        public NetworkServerType serverType = NetworkServerType.DEVELOPMENT;
        public NetworkServerPlatform platform = NetworkServerPlatform.BUKKIT;
    }
}
