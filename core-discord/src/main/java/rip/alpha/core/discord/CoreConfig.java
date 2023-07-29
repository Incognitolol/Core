package rip.alpha.core.discord;

import lombok.Getter;
import rip.alpha.libraries.configuration.Configuration;
import rip.alpha.libraries.configuration.Configurations;
import rip.alpha.core.shared.ranks.Rank;
import rip.alpha.libraries.logging.LogLevel;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CoreConfig implements Configuration {
    @Getter
    private static final transient CoreConfig instance = Configurations.computeIfAbsent(new CoreConfig());

    @Getter
    private final LogLevel logLevel = LogLevel.BASIC;

    @Getter
    private final DiscordConfig discordConfig = new DiscordConfig();
    @Override
    public File getFileLocation() {
        return new File("config", "config.json");
    }

    @Getter
    public static class DiscordConfig {
        private String token = "?";
        private List<String> allowedGuilds = List.of("779939807761530910", "771531354801831996", "404397775514632193", "962146276525350983");
        private String syncDiscord = "779939807761530910"; //Only try to give ranks in this discord cause we use role ids.
        private Map<Rank, String> discordRoles  = new HashMap<>() {{
            this.put(Rank.DEFAULT, "962924764278689812");
            this.put(Rank.VIP, "780136392163721227");
            this.put(Rank.VIP_PLUS, "780136392163721227");
            this.put(Rank.PRO, "780136390582468649");
            this.put(Rank.PRO_PLUS, "780136390582468649");
            this.put(Rank.MVP, "780136375491100715");
            this.put(Rank.MVP_PLUS, "780136375491100715");
            this.put(Rank.HOF, "780136375131176970");
        }};
    }

}
