package rip.alpha.core.discord.command.commands.player;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import rip.alpha.core.discord.CoreBot;
import rip.alpha.core.discord.command.GenericCommand;
import rip.alpha.core.discord.command.util.CommandContext;
import rip.alpha.core.discord.reaction.data.ReactionRole;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * @author Moose1301
 * @date 4/13/2022
 */
public class ReactionRoleCommand extends GenericCommand {
    public ReactionRoleCommand() {
        super("reactionrole", "Change the Reaction Role Settings", true, true);
    }

    @Override
    public void execute(CommandContext ctx) {
        if(!ctx.hasPermission(Permission.MANAGE_SERVER)) {
            ctx.reply("No Permission");
            return;
        }
        JDA jda = ctx.getJDA();
        Guild guild = ctx.getGuild();
        String subCommand = ctx.getEvent().getSubcommandName();
        if(subCommand != null) {
            if (subCommand.equalsIgnoreCase("help")) {
                ctx.reply("TODO Help Docs");
            } else if (subCommand.equalsIgnoreCase("list")) {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setTitle(CoreBot.getInstance().getReactionHandler().getReactionRoles().getAllLoadedKeys().size() + " Registered Reaction Roles");
                StringBuilder sb = new StringBuilder();
                for (ReactionRole reactionRole : CoreBot.getInstance().getReactionHandler().getReactionRoles().getAllLoadedValues()) {
                    sb.append("Message: " + reactionRole.getMessageId() + "\n");
                    sb.append("Roles: \n");
                    for (Map.Entry<String, Long> entry : reactionRole.getRoles().entrySet()) {
                        String emote = entry.getKey();
                        if(emote.contains(":")) {
                            emote = "<:" + emote + ">";
                        }
                        sb.append("    - " + emote + " " + guild.getRoleById(entry.getValue()).getName() + "\n");
                    }
                }
                builder.setDescription(sb.toString());
                ctx.reply(builder.build());
            }
            else if (subCommand.equalsIgnoreCase("add")) {
                if(ctx.getOption("channel").getChannelType() != ChannelType.TEXT) {
                    ctx.reply("That channel isnt a text channel");
                    return;
                }
                ctx.deferReply();

                TextChannel channel = ctx.getOption("channel").getAsTextChannel();
                Message message;
                try {
                   message = channel.retrieveMessageById(ctx.getOption("message").getAsLong()).complete();
                } catch (Exception ex) {
                    ctx.reply("Could not get message.");
                    return;
                }
                Role role = ctx.getOption("role").getAsRole();

                String emote;
                try {
                    String emoteName = ctx.getOption("emote").getAsString().replace(":", "");

                    boolean custom = emoteName.contains("<");
                    if (custom) {
                        String customEmote = emoteName.replace("<", "").replace(">", "").replaceAll("[^0-9]", "");
                        Emote jEmote  = jda.getEmoteById(customEmote);
                        emote = jEmote.getName() + ":" + jEmote.getId();
                    } else  {
                        emote = emoteName;
                    }

                } catch (Exception ex) {
                    ctx.reply("Couldn't get the emote you inputted is it from another discord?");
                    return;
                }
                ReactionRole reactionRole = CoreBot.getInstance().getReactionHandler().getReactionRoleByMessage(message.getIdLong());
                if(reactionRole == null) {
                    reactionRole = CoreBot.getInstance().getReactionHandler().getReactionRoles().loadAndGetSync(UUID.randomUUID());
                    reactionRole.setRoles(new HashMap<>());
                    reactionRole.setGuildId(message.getGuild().getIdLong());
                    reactionRole.setChannelId(message.getChannel().getIdLong());
                    reactionRole.setMessageId(message.getIdLong());
                }

                ReactionRole finalReactionRole = reactionRole;
                message.addReaction(emote).queue(new Consumer<Void>() {
                    @Override
                    public void accept(Void success) {
                        finalReactionRole.getRoles().put(emote, role.getIdLong());

                        ctx.reply("Created Reaction Role");
                    }
                }, (e) -> {
                    if (e.getMessage().contains("Unknown Emoji"))
                        ctx.reply("Failed to setup reaction role!\nDid you provide a valid emoji/emote?");

                });
            }
            else if (subCommand.equalsIgnoreCase("remove")) {
                if(ctx.getOption("channel").getChannelType() != ChannelType.TEXT) {
                    ctx.reply("That channel isnt a text channel");
                    return;
                }
                ctx.deferReply();

                TextChannel channel = ctx.getOption("channel").getAsTextChannel();
                Message message;
                try {
                    message = channel.retrieveMessageById(ctx.getOption("message").getAsLong()).complete();
                } catch (Exception ex) {
                    ctx.reply("Could not get message.");
                    return;
                }
                String emote;
                try {
                    String emoteName = ctx.getOption("emote").getAsString().replace(":", "");

                    boolean custom = emoteName.contains("<");
                    if (custom) {
                        String customEmote = emoteName.replace("<", "").replace(">", "").replaceAll("[^0-9]", "");
                        Emote jEmote  = jda.getEmoteById(customEmote);
                        emote = jEmote.getName() + ":" + jEmote.getId();
                    } else  {
                        emote = emoteName;
                    }

                } catch (Exception ex) {
                    ctx.reply("Couldn't get the emote you inputted is it from another discord?");
                    return;
                }
                ReactionRole reactionRole = CoreBot.getInstance().getReactionHandler().getReactionRoleByMessage(message.getIdLong());
                if(reactionRole == null) {
                    ctx.reply("Unknown Reaction Role for that Message!");
                    return;
                }

                message.removeReaction(emote).queue(success -> {
                    reactionRole.getRoles().remove(emote);
                    if(reactionRole.getRoles().isEmpty()) {
                        CoreBot.getInstance().getReactionHandler().getReactionRoles().deleteDataGloballyAsync(reactionRole.getId());
                    }
                    ctx.reply("Deleted Reaction Role");
                }, (e) -> {
                    if (e.getMessage().contains("Unknown Emoji"))
                        ctx.reply("Failed to delete reaction role!\nDid you provide a valid emoji/emote?");

                });
            }
        }
    }

    @Override
    public CommandData register(JDA jda) {
        return new CommandDataImpl(name, description)
                .addSubcommands(
                        new SubcommandData("help", "Show the help Documents"),
                        new SubcommandData("list", "List all the reaction roles registered in this discord"),
                        new SubcommandData("add",  "Add a reaction role to add message")
                                .addOption(OptionType.CHANNEL, "channel", "The channel this message is in", true)
                                .addOption(OptionType.STRING, "message", "The message you want to add this too", true)
                                .addOption(OptionType.ROLE, "role", "The role that you want to add give for this reaction", true)
                                .addOption(OptionType.STRING, "emote", "The emote you want to add to this reaction", true),
                        new SubcommandData("remove", "Remove a reaction role from a message")
                                .addOption(OptionType.CHANNEL, "channel", "The channel this message is in", true)
                                .addOption(OptionType.STRING, "message", "The message you want to add this too", true)
                                .addOption(OptionType.STRING, "emote", "The emote you want to add to this reaction", true)
                );
    }
}