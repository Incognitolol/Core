package rip.alpha.core.voter;

import lombok.Getter;
import rip.alpha.core.shared.ranks.Rank;
import rip.alpha.core.voter.utils.RSAKeygen;
import rip.alpha.libraries.configuration.Configuration;
import rip.alpha.libraries.configuration.Configurations;
import rip.alpha.libraries.logging.LogLevel;

import java.io.File;
import java.security.Key;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Moose1301
 * @date 4/19/2022
 */
public class CoreConfig implements Configuration {
    @Getter
    private static final transient CoreConfig instance = Configurations.computeIfAbsent(new CoreConfig());

    @Getter
    private final LogLevel logLevel = LogLevel.BASIC;

    @Getter
    private final int startingCoins = 100; //$0.10
    @Getter
    private final int maxCoins = 1000; //$1
    @Getter
    private Map<String, String> tokens  = new HashMap<>() {{
        this.put("default", RSAKeygen.newToken());
    }};

    @Override
    public File getFileLocation() {
        return new File("config", "config.json");
    }
}
