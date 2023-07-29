package rip.alpha.core.discord.command;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import rip.alpha.core.discord.CoreBot;
import rip.alpha.core.discord.command.commands.developer.TimeTrackerCommand;
import rip.alpha.core.discord.command.commands.player.ReactionRoleCommand;
import rip.alpha.core.discord.command.commands.player.SyncCommand;
import rip.alpha.core.discord.command.commands.staff.BugReportCommand;
import rip.alpha.core.discord.command.commands.staff.LinksCommand;
import rip.alpha.core.discord.command.commands.staff.SuggestionCommand;
import rip.alpha.core.discord.command.util.CommandContext;
import rip.alpha.libraries.logging.LogLevel;

import java.util.*;

public class CommandManager extends ListenerAdapter {
    public List<GenericCommand> commands = new ArrayList<>();
    public JDA jda;
    public CommandManager(JDA jda) {
        this.jda = jda;
        register(
                //Public Discord Commands
                new SyncCommand(),
                new ReactionRoleCommand(),

                //Staff Discord Commands
                new LinksCommand(),
                new SuggestionCommand(),
                new BugReportCommand(),

                //Developer Commands
                new TimeTrackerCommand()
        );
    }
    public void register(GenericCommand... commands) {
        List<CommandData> staffDiscord = new ArrayList<>();
        List<CommandData> publicDiscord = new ArrayList<>();
        for (GenericCommand command : commands) {
            this.commands.add(command);
            if(command.staffDiscord) {
                staffDiscord.add(command.register(jda));
            }
            if(command.publicDiscord) {
                publicDiscord.add(command.register(jda));
            }
        }
        //Lets not get rate limited just from registering commands!!

        jda.getGuildById("962146276525350983").updateCommands().addCommands(staffDiscord).queue();
            jda.getGuildById("779939807761530910").updateCommands().addCommands(publicDiscord).queue();
        CoreBot.LOGGER.log("Registered: " + publicDiscord.size() + " Public Discord Commands and "
                + staffDiscord.size() + " Staff Discord Commands", LogLevel.BASIC);

    }

    public void unregister(GenericCommand... commands) {
        this.commands.removeAll(Arrays.asList(commands));
    }
    @SubscribeEvent
    public void onMessageReceived(MessageReceivedEvent event) {
        Member member = event.getMember();
        if(member == null) return;
        if (!CoreBot.DEVELOPERS.contains(member.getId())) {
            return;
        }
        String message = event.getMessage().getContentRaw();
        if(message.equalsIgnoreCase("/save")) {
            CoreBot.getInstance().save();
            CoreBot.LOGGER.log("Forcefully saving all Data", LogLevel.BASIC);
        }
    }

    @SubscribeEvent
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {

        String commandID = event.getName();
        for(GenericCommand command : commands) {
            if(command.getName().equalsIgnoreCase(commandID)) {
                CommandContext context = new CommandContext(event);
                if(command.isDeveloper()) {
                    Member member = context.getMember();
                    if (!CoreBot.DEVELOPERS.contains(member.getId())) {
                        context.reply("No Permission");
                        return;
                    }
                    command.execute(context);
                } else {
                    command.execute(context);
                }

            }
        }
    }

}