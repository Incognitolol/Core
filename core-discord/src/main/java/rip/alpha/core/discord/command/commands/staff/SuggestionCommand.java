package rip.alpha.core.discord.command.commands.staff;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import rip.alpha.core.discord.command.GenericCommand;
import rip.alpha.core.discord.command.util.CommandContext;

import java.awt.*;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.function.Consumer;

/**
 * @author Moose1301
 * @date 4/13/2022
 */
public class SuggestionCommand extends GenericCommand {
    public SuggestionCommand() {
        super("suggest", "Suggest A Thing", false, true);
    }

    @Override
    public void execute(CommandContext ctx) {
        Guild guild = ctx.getGuild();
        Member member = ctx.getMember();
        JDA jda = ctx.getJDA();
        String suggestion = ctx.getEvent().getOption("suggestion").getAsString();
        TextChannel suggestChannel = guild.getTextChannelById("962333538940776528");
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(Color.GREEN)
                .setTimestamp(OffsetDateTime.ofInstant(date.toInstant(), ZoneId.of("America/New_York")))
                .setFooter(calendar.get(Calendar.MONTH) + "/" + calendar.get(Calendar.DAY_OF_WEEK) + "/" + calendar.get(Calendar.YEAR))
                .setThumbnail(member.getUser().getAvatarUrl())
                .setDescription("**Suggestion**\n" + suggestion +
                        "\n\n**Submitter**\n" + member.getUser().getName() + "#" + member.getUser().getDiscriminator());
        ctx.reply("Suggestion Sent");
        suggestChannel.sendMessageEmbeds(embedBuilder.build()).queue(new Consumer<Message>() {
            @Override
            public void accept(Message message) {
                //780232283859976224 = Yes https://cdn.discordapp.com/emojis/780232283859976224.png
                //780232284141781003 = No https://cdn.discordapp.com/emojis/780232284141781003.png
                message.addReaction(jda.getEmoteById("780232283859976224")).queue();
                message.addReaction(jda.getEmoteById("780232284141781003")).queue();
            }
        });
    }

    @Override
    public CommandData register(JDA jda) {
        return new CommandDataImpl(name, description)
                .addOption(OptionType.STRING, "suggestion", "Suggestion You have", true, false);
    }
}
