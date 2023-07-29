package rip.alpha.core.discord.bridge;

import net.dv8tion.jda.api.EmbedBuilder;
import rip.alpha.core.discord.utils.DiscordLog;
import rip.alpha.core.shared.punishments.ProfilePunishmentAddEvent;
import rip.alpha.core.shared.punishments.ProfilePunishmentRemoveEvent;
import rip.alpha.core.shared.punishments.Punishment;
import rip.alpha.libraries.util.TimeUtil;
import rip.alpha.libraries.util.data.NameCache;

import java.awt.*;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.function.Consumer;

/**
 * @author Moose1301
 * @date 4/9/2022
 */
public class PunishmentMessageListener {
    public static void onPunishmentAdd(ProfilePunishmentAddEvent event) {
       NameCache.getInstance().getNameAsync(event.profileID()).thenAccept(new Consumer<String>() {
           @Override
           public void accept(String name) {
               Punishment punishment = event.punishment();
               Punishment.Type type = punishment.type();
               EmbedBuilder embedBuilder = new EmbedBuilder();
               embedBuilder.setTitle("Punishment Create");
               embedBuilder.setColor(Color.RED);
               embedBuilder.setTimestamp(OffsetDateTime.ofInstant(new Date(punishment.start()).toInstant(), ZoneId.of("America/New_York")));
               StringBuilder sb = new StringBuilder();
               sb.append("Creator: " + punishment.sender() + "\n");
               sb.append("Target: " + name + "\n");
               sb.append("Type: " + type.getTypeName() + "\n");
               if(punishment.isInfinite()) {
                   sb.append("Duration: " + "Permanent" + "\n");
               } else {
                   sb.append("Duration: " + TimeUtil.formatIntoDetailedString((int) (punishment.getTimeLeft() / 1000L)) + "\n");
               }
               sb.append("Reason: " + punishment.reason() + "\n");
               embedBuilder.setDescription(sb.toString());
               DiscordLog.coreLog(embedBuilder);
           }
       });

    }
    public static void onPunishmentRemoved(ProfilePunishmentRemoveEvent event) {
        NameCache.getInstance().getNameAsync(event.profileID()).thenAccept(new Consumer<String>() {
            @Override
            public void accept(String name) {
                Punishment punishment = event.punishment();
                Punishment.Type type = punishment.type();
                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setTitle("Punishment Remove");
                embedBuilder.setColor(Color.RED);
                embedBuilder.setTimestamp(OffsetDateTime.ofInstant(new Date(System.currentTimeMillis()).toInstant(), ZoneId.of("America/New_York")));
                StringBuilder sb = new StringBuilder();
                sb.append("Remover: " + event.removedBy() + "\n");
                sb.append("Target: " + name + "\n");
                sb.append("Type: " + type.getTypeName() + "\n");
                sb.append("Reason: " + event.removeReason() + "\n");
                embedBuilder.setDescription(sb.toString());
                DiscordLog.coreLog(embedBuilder);
            }
        });

    }
}
