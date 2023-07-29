package rip.alpha.core.discord.command.commands.player;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import rip.alpha.core.discord.CoreBot;
import rip.alpha.core.discord.CoreConfig;
import rip.alpha.core.discord.command.GenericCommand;
import rip.alpha.core.discord.command.util.CommandContext;
import rip.alpha.core.shared.data.AlphaProfileManager;
import rip.alpha.core.shared.grants.Grant;
import rip.alpha.core.shared.ranks.Rank;
import rip.alpha.libraries.util.data.NameCache;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * @author Moose1301
 * @date 4/10/2022
 */
public class SyncCommand extends GenericCommand {
    public SyncCommand() {
        super("sync", "Sync your Minecraft Account to your Discord Account", true, true);
    }

    @Override
    public void execute(CommandContext ctx) {
        String code = ctx.getEvent().getOption("code").getAsString();
        ctx.deferReply();
        UUID profileID = CoreBot.getInstance().getSyncRankHandler().getProfile(code);
        if(profileID == null) {
            ctx.reply("Invalid Code. Please get one in game by doing /sync.");
            return;
        }
        AlphaProfileManager.profiles().applyToData(profileID, profile -> {
            profile.setDiscordId(ctx.getEvent().getUser().getId());
           NameCache.getInstance().getNameAsync(profileID).thenAccept(new Consumer<String>() {
                @Override
                public void accept(String name) {
                    ctx.reply("Successfully synced to " + name);
                }
            });
            CoreBot.getInstance().getSyncRankHandler().removeSyncCode(code);
            if(!ctx.getGuild().getId().equalsIgnoreCase(CoreConfig.getInstance().getDiscordConfig().getSyncDiscord())) {
                return;
            }
            String syncRole = CoreConfig.getInstance().getDiscordConfig().getDiscordRoles().getOrDefault(Rank.DEFAULT, null);
            if(syncRole != null) {
                ctx.getGuild().addRoleToMember(ctx.getMember(), ctx.getGuild().getRoleById(syncRole)).reason("Rank Sync").queue();
            }

            for (Grant grant : profile.getAllGrants()) {
                String roleId = CoreConfig.getInstance().getDiscordConfig().getDiscordRoles().getOrDefault(grant.rank(), null);
                if(roleId == null) {
                    continue;
                }
                ctx.getGuild().addRoleToMember(ctx.getMember(), ctx.getGuild().getRoleById(roleId)).reason("Rank Sync").queue();
            }


        });
    }

    @Override
    public CommandData register(JDA jda) {
        return new CommandDataImpl(name, description).
                addOption(OptionType.STRING, "code", "Your Sync Code", true);
    }
}
