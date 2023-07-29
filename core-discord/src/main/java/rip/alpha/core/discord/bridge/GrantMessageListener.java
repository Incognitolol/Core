package rip.alpha.core.discord.bridge;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import rip.alpha.core.discord.CoreBot;
import rip.alpha.core.discord.CoreConfig;
import rip.alpha.core.discord.utils.CoreLangauage;
import rip.alpha.core.discord.utils.DiscordLog;
import rip.alpha.core.shared.data.AlphaProfile;
import rip.alpha.core.shared.data.AlphaProfileManager;
import rip.alpha.core.shared.grants.Grant;
import rip.alpha.core.shared.grants.ProfileGrantAddEvent;
import rip.alpha.core.shared.grants.ProfileGrantRemoveEvent;
import rip.alpha.core.shared.ranks.Rank;
import rip.alpha.libraries.util.TimeUtil;
import rip.alpha.libraries.util.data.NameCache;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.function.Consumer;

/**
 * @author Moose1301
 * @date 4/9/2022
 */
public class GrantMessageListener {
    public static void onGrantGained(ProfileGrantAddEvent event) {
        Grant grant = event.grant();
        NameCache.getInstance().getNameAsync(event.profileID()).thenAccept(new Consumer<String>() {
            @Override
            public void accept(String name) {

                Rank rank = event.grant().rank();
                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setTitle(CoreLangauage.getInstance().getGrantLanguage().getAdded().getTitle());
                embedBuilder.setTimestamp(OffsetDateTime.ofInstant(new Date().toInstant(), ZoneId.of("America/New_York")));
                embedBuilder.setColor(CoreLangauage.getInstance().getGrantLanguage().getAdded().getColor());
                embedBuilder.setDescription(CoreLangauage.getInstance().getGrantLanguage().getAdded().getDescription()
                        .replace("{user}", grant.sender())
                        .replace("{target}", name)
                        .replace("{rank}", rank.getName())
                        .replace("{duration}", (grant.getTimeLeft() == 9223372036854775807L) ? "Permanent" : TimeUtil.formatTime(grant.getTimeLeft()))
                        .replace("{reason}", grant.reason())
                );
                DiscordLog.coreLog(embedBuilder);
            }
        });


        AlphaProfileManager.profiles().applyToData(event.profileID(), new Consumer<AlphaProfile>() {
            @Override
            public void accept(AlphaProfile profile) {
                if (profile.getDiscordId() == null) {
                    return;
                }
                Guild guild = CoreBot.getInstance().getJda().getGuildById(CoreConfig.getInstance().getDiscordConfig().getSyncDiscord());
                String roleId = CoreConfig.getInstance().getDiscordConfig().getDiscordRoles().getOrDefault(grant.rank(), null);
                if (roleId == null) {
                    return;
                }
                Member member = guild.getMemberById(profile.getDiscordId());
                guild.addRoleToMember(member, guild.getRoleById(roleId)).reason("Rank Sync").queue();
            }
        });

    }

    public static void onGrantLost(ProfileGrantRemoveEvent event) {
        Grant grant = event.grant();
        NameCache.getInstance().getNameAsync(event.profileID()).thenAccept(name -> {
            Rank rank = event.grant().rank();
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle(CoreLangauage.getInstance().getGrantLanguage().getRemoved().getTitle());
            embedBuilder.setColor(CoreLangauage.getInstance().getGrantLanguage().getRemoved().getColor());
            embedBuilder.setTimestamp(OffsetDateTime.ofInstant(new Date().toInstant(), ZoneId.of("America/New_York")));
            embedBuilder.setDescription(CoreLangauage.getInstance().getGrantLanguage().getRemoved().getDescription()
                    .replace("{remover}", (event.reason() == ProfileGrantRemoveEvent.Reason.TAKEN ? event.removedBy() : "Automatic"))
                    .replace("{target}", name)
                    .replace("{rank}", rank.getName())
                    .replace("{duration}", (grant.getTimeLeft() == 9223372036854775807L) ? "Permanent" : TimeUtil.formatTime(grant.getTimeLeft()))
                    .replace("{reason}", event.removeReason())
                    .replace("{removeType}", event.reason().name().replace("_", " "))
            );
            DiscordLog.coreLog(embedBuilder);
        });
        AlphaProfileManager.profiles().applyToData(event.profileID(), profile -> {
            if (profile.getDiscordId() == null) {
                return;
            }
            Guild guild = CoreBot.getInstance().getJda().getGuildById(CoreConfig.getInstance().getDiscordConfig().getSyncDiscord());
            String roleId = CoreConfig.getInstance().getDiscordConfig().getDiscordRoles().getOrDefault(grant.rank(), null);
            if (roleId == null) {
                return;
            }
            Member member = guild.getMemberById(profile.getDiscordId());
            guild.removeRoleFromMember(member, guild.getRoleById(roleId)).reason("Rank Sync").queue();
        });
    }


}

