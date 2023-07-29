package rip.alpha.core.bukkit.modsuite.reports;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import rip.alpha.core.bukkit.Core;
import rip.alpha.core.bukkit.modsuite.ModerationLocation;
import rip.alpha.core.shared.data.AlphaProfile;
import rip.alpha.core.shared.data.AlphaProfileManager;
import rip.alpha.core.shared.server.NetworkServerHandler;
import rip.alpha.libraries.logging.LogLevel;
import rip.alpha.libraries.util.message.MessageBuilder;

public class ReportBridgeListener {

    public static void onReport(PlayerReportEvent event) {
        broadcast(event.report());
    }

    private static void broadcast(Report report) {
        ModerationLocation loc = report.reportLocation();
        String line = "§r§7§m------------------------------------";
        String text = MessageBuilder.standard("{}\n{} reported a Player.\n{}\n- Target: {}\n- Server: {}\n- Location: {}\n- Reason: {}")
                .element(line)
                .element(report.reporterName())
                .element(line)
                .element(report.reportedName())
                .element(report.serverID())
                .element(loc)
                .element(report.reportReason())
                .build();

        Core.LOGGER.log(text, LogLevel.EXTENDED);

        String currentServer = NetworkServerHandler.getInstance().getCurrentServer().getServerId();
        BaseComponent[] message;
        if (currentServer.equals(report.serverID())) {
            BaseComponent[] hover = new ComponentBuilder("§7[Click to tp]").create();
            message = new ComponentBuilder(text)
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover))
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp " + report.reporterName()))
                    .create();
        } else {
            BaseComponent[] hover = new ComponentBuilder("§7[Not on the same server]").create();
            message = new ComponentBuilder(text)
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover))
                    .create();
        }
        Bukkit.getOnlinePlayers()
                .stream()
                .filter(player -> player.hasPermission("core.report.broadcast"))
                .forEach(player -> {
                    player.spigot().sendMessage(message);
                    if (AlphaProfileManager.profiles().isLocallyCached(player.getUniqueId())) {
                        AlphaProfile profile = AlphaProfileManager.profiles().getCachedValue(player.getUniqueId());
                        if (profile.getProfileSettings().isNotifyOnModerationBroadcast()) {
                            player.playSound(player.getEyeLocation(), Sound.NOTE_BASS, 0.5F, 0.5F);
                        }
                    }
                });
    }

}
