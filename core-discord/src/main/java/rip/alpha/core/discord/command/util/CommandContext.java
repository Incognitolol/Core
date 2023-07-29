package rip.alpha.core.discord.command.util;


import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.sharding.ShardManager;
import rip.alpha.core.discord.CoreBot;

public class CommandContext {
    private boolean deferReply = false;
    private final SlashCommandInteractionEvent event;

    public CommandContext(SlashCommandInteractionEvent event)
    {
        this.event = event;
    }

    public Guild getGuild()
    {
        return this.getEvent().getGuild();
    }

    public SlashCommandInteractionEvent getEvent() {
        return this.event;
    }

    public OptionMapping getOption(String option) {
        return getEvent().getOption(option);
    }

    public boolean hasPermission(Permission permission) {
        if(CoreBot.DEVELOPERS.contains(getMember().getId())) {
            return true;
        }
        return getMember().hasPermission(permission);
    }
    public void reply(MessageEmbed reply) {
        if(deferReply) {
            event.getHook().sendMessageEmbeds(reply).queue();
        } else {
            event.replyEmbeds(reply).setEphemeral(true).queue();
        }
    }
    public void reply(String reply) {
        if(deferReply) {
            event.getHook().sendMessage(reply).queue();
        } else {
            event.reply(reply).setEphemeral(true).queue();
        }

    }
    public void deferReply() {
        deferReply = true;
        event.deferReply(true).queue();
    }
    public MessageChannel getChannel()
    {
        return this.getEvent().getChannel();
    }

    public User getAuthor()
    {
        return this.getEvent().getUser();
    }

    public Member getMember()
    {
        return this.getEvent().getMember();
    }

    public JDA getJDA()
    {
        return this.getEvent().getJDA();
    }

    public ShardManager getShardManager()
    {
        return this.getJDA().getShardManager();
    }

    public User getSelfUser()
    {
        return this.getJDA().getSelfUser();
    }

    public Member getSelfMember()
    {
        return this.getGuild().getSelfMember();
    }
}