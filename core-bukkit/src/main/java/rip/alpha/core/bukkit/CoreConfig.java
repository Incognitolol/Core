package rip.alpha.core.bukkit;

import lombok.Getter;
import rip.alpha.core.bukkit.util.math.MathematicalExpression;
import rip.alpha.libraries.configuration.Configuration;
import rip.alpha.libraries.configuration.Configurations;
import rip.alpha.libraries.logging.LogLevel;

import java.io.File;

public class CoreConfig implements Configuration {

    @Getter
    private static final transient CoreConfig instance = Configurations.computeIfAbsent(new CoreConfig());

    @Getter
    private final LogLevel logLevel = LogLevel.BASIC;

    @Getter
    private final MathematicalExpression levelExpression = new MathematicalExpression("lvl ^ 2 + 50 * lvl + 100");

    @Override
    public File getFileLocation() {
        return new File(Core.getInstance().getDataFolder(), "config.json");
    }
}
