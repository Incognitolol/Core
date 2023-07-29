package rip.alpha.core.discord.command.commands.staff;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import rip.alpha.core.discord.command.GenericCommand;
import rip.alpha.core.discord.command.util.CommandContext;

/**
 * @author Moose1301
 * @date 4/14/2022
 */
public class SSRequestCommand extends GenericCommand {
    public SSRequestCommand() {
        super("ss", "Submit a SS Request for a player!", false, true);
    }

    @Override
    public void execute(CommandContext ctx) {
        String username = ctx.getOption("username").getAsString();
        String reason = ctx.getOption("reason").getAsString();
        String proof = "No Proof Provided";
        OptionMapping proofOption = ctx.getOption("proof");
        if(proofOption != null) {
            proof = proofOption.getAsString();
        }
    }

    @Override
    public CommandData register(JDA jda) {
        return new CommandDataImpl(name, description)
                .addOption(OptionType.STRING, "username", "The username the person your reporting", true)
                .addOption(OptionType.STRING, "reason", "The reason your submitting a SS Request for this player", true)
                .addOption(OptionType.STRING, "proof", "The proof for this SS Request", false);
    }
}
