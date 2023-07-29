package rip.alpha.core.bukkit.modsuite.reports;

import org.bukkit.entity.Player;
import rip.alpha.core.bukkit.data.server.GlobalServerDataManager;
import rip.alpha.core.bukkit.modsuite.ModerationLocation;
import rip.alpha.core.shared.data.AlphaProfile;
import rip.alpha.core.shared.data.AlphaProfileManager;
import rip.alpha.core.shared.server.NetworkServerHandler;
import rip.alpha.libraries.command.annotation.Command;
import rip.alpha.libraries.command.annotation.CommandUsage;
import rip.alpha.libraries.command.annotation.Wildcard;
import rip.alpha.libraries.util.message.MessageBuilder;

import java.util.UUID;

public class ReportCommands {

    @CommandUsage("<target> <reason>")
    @Command(names = {" report"}, async = true)
    public static void onReport(Player sender, String name, @Wildcard String reason) {
        sender.sendMessage(MessageBuilder.construct("Sending report..."));
        UUID repID = sender.getUniqueId();
        String repName = sender.getName();
        ModerationLocation loc = ModerationLocation.of(sender.getLocation());
        String serverID = NetworkServerHandler.getInstance().getCurrentServer().getServerId();
        Report report = new Report(repID, repName, loc, serverID, name, reason);
        GlobalServerDataManager.servers()
                .applyToDataAsync(serverID, data -> data.getReportData().addReport(report))
                .thenRun(() -> new PlayerReportEvent(report).callEvent())
                .thenRun(() -> sender.sendMessage(MessageBuilder.construct("Report was sent.")));
    }

    @Command(names = {"reports", "reportmenu"}, permission = "core.command.reports")
    public static void onReportMenu(Player sender) {
        ReportMenu.openFor(sender);
    }

}
