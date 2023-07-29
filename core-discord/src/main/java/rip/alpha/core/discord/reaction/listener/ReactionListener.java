package rip.alpha.core.discord.reaction.listener;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import rip.alpha.core.discord.CoreBot;
import rip.alpha.core.discord.reaction.data.ReactionRole;
import rip.alpha.libraries.logging.LogLevel;

/**
 * @author Moose1301
 * @date 4/13/2022
 */
public class ReactionListener extends ListenerAdapter {
    private Role role;
    public ReactionListener(JDA jda) {
        role = jda.getRoleById("843634064820011008");
    }
    @SubscribeEvent
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        MessageReaction.ReactionEmote reactionEmote = event.getReaction().getReactionEmote();
        Member member = event.getMember();
        if(member == null || member.getUser().isBot()) {
            return;
        }

        Guild guild = event.getGuild();
        if(event.getMessageId().equals("909140299970912286") && event.getReactionEmote().getName().equalsIgnoreCase("Alpha")) {
            guild.addRoleToMember(member, role).queue();
            return;
        }
        String emoji;
        if (reactionEmote.isEmote())
            emoji = reactionEmote.getEmote().getName() + ":" + reactionEmote.getEmote().getId();
        else
            emoji = reactionEmote.getEmoji();
        ReactionRole reactionRole = CoreBot.getInstance().getReactionHandler().getReactionRoleByMessage(event.getMessageIdLong());
        if(reactionRole == null) {
            return;
        }
        if(reactionRole.getGuildId() != guild.getIdLong()) {
            CoreBot.LOGGER.warn("Reaction Role with ID: " + reactionRole.getId() + " was called from different guild then the registered in", LogLevel.BASIC);
            return;

        }
        Long roleId = reactionRole.getRoles().getOrDefault(emoji, null);
        if(roleId == null) {
            return;
        }
        Role role = guild.getRoleById(roleId);
        if (guild.getSelfMember().hasPermission(Permission.MESSAGE_MANAGE)) {
            event.retrieveMessage().queue(message -> message.removeReaction(emoji, member.getUser()).queue());
        }
        if (guild.getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
            if(member.getRoles().contains(role)) {
                guild.addRoleToMember(member, role).queue();
            } else {
                guild.removeRoleFromMember(member, role).queue();
            }
        }

    }
}
