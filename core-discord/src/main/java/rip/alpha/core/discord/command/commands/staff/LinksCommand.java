package rip.alpha.core.discord.command.commands.staff;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import rip.alpha.core.discord.command.GenericCommand;
import rip.alpha.core.discord.command.util.CommandContext;

/**
 * @author Moose1301
 * @date 4/11/2022
 */
public class LinksCommand extends GenericCommand {
    public LinksCommand() {
        super("links", "Get the Links for helping you staff!", false, true);
    }

    @Override
    public void execute(CommandContext ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append("Staff Guide: <Add Staff Guide Here>\n");
        sb.append("Punishment Guide: <Add Punishment Guide Here>\n");
        sb.append("Tickets: https://alpha.rip/staff-tickets");

        ctx.reply(sb.toString());
    }

    @Override
    public CommandData register(JDA jda) {
        return new CommandDataImpl(name, description);
    }
}
