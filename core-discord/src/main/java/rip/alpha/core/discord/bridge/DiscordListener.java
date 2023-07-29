package rip.alpha.core.discord.bridge;

import net.dv8tion.jda.api.EmbedBuilder;
import rip.alpha.core.discord.utils.DiscordLog;
import rip.alpha.core.shared.discord.DiscordLogEvent;

/**
 * @author Moose1301
 * @date 4/15/2022
 */
public class DiscordListener {
    public static void onDiscordLog(DiscordLogEvent event) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(event.title());
        embedBuilder.setColor(event.color());
        embedBuilder.setDescription(event.description());
        embedBuilder.setFooter("Server: " + event.serverId());
        DiscordLog.coreLog(embedBuilder);
    }
}
