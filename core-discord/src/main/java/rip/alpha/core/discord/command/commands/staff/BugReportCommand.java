package rip.alpha.core.discord.command.commands.staff;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import rip.alpha.core.discord.command.GenericCommand;
import rip.alpha.core.discord.command.util.CommandContext;

/**
 * @author Moose1301
 * @date 4/13/2022
 */
public class BugReportCommand extends GenericCommand {
    public BugReportCommand() {
        super("bug", "Report a bug to the developers", false, true);
    }

    @Override
    public void execute(CommandContext ctx) {
        Member member = ctx.getMember();
        Guild guild = ctx.getGuild();
        String link = ctx.getOption("link").getAsString();
        String report = ctx.getOption("report").getAsString();
        TextChannel channel = guild.getTextChannelById("963934348325842984");
        StringBuilder sb = new StringBuilder();
        sb.append("Bug Report from: " + member.getEffectiveName() + "\n");
        sb.append("Link: " + link + "\n");
        sb.append("Description: " + report);
        channel.sendMessage(sb.toString()).queue();
        ctx.reply("Reported bug to developers");
    }

    @Override
    public CommandData register(JDA jda) {
        return new CommandDataImpl(name, description)
                .addOption(OptionType.STRING, "link", "A VIDEO or GIF that EXPLAINS the bug", true)
                .addOption(OptionType.STRING, "report", "A description of the bug", true);
    }
}
