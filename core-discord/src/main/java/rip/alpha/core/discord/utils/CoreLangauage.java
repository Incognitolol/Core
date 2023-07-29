package rip.alpha.core.discord.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import rip.alpha.libraries.configuration.Configuration;
import rip.alpha.libraries.configuration.Configurations;

import java.awt.*;
import java.io.File;

/**
 * @author Moose1301
 * @date 4/11/2022
 */
public class CoreLangauage implements Configuration {
    @Getter
    private static final transient CoreLangauage instance = Configurations.computeIfAbsent(new CoreLangauage());

    @Override
    public File getFileLocation() {
        return new File("config", "lang.json");
    }


    @Getter
    private final GrantLanguage grantLanguage = new GrantLanguage();
    private final PunishmentLanguage punishmentLanguage = new PunishmentLanguage();

    @Getter
    public class GrantLanguage {
        private EmbedLanguage added = new EmbedLanguage(Color.GREEN, "Grant Added", """
                Granter: {user}
                Target: {target}
                Rank: {rank}
                Reason: {reason}
                Duration {duration}
                """);
        private EmbedLanguage removed = new EmbedLanguage(Color.RED, "Grant Removed", """
                Remover: {remover}
                Target: {target}
                Rank: {rank}
                Reason: {reason}
                Duration {duration}
                Remove Type: {removeType}
                """);
    }
    @Getter
    public class PunishmentLanguage {

    }
    @Getter @AllArgsConstructor @NoArgsConstructor
    public class EmbedLanguage {
        private Color color;
        private String title;
        private String description;
    }
}
