package rip.alpha.core.discord.utils;

import net.dv8tion.jda.api.EmbedBuilder;
import rip.alpha.core.discord.CoreBot;

/**
 * @author Moose1301
 * @date 4/9/2022
 */
public class DiscordLog {
    public static final String GUILD_ID = "962146276525350983";
    public static final String CORE_LOG = "962329627127320576";
    public static void coreLog(EmbedBuilder builder) {
        CoreBot.getInstance().getJda().getGuildById(GUILD_ID).getTextChannelById(CORE_LOG).sendMessageEmbeds(builder.build()).queue();
    }
}
