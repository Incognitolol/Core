package rip.alpha.core.bukkit.modsuite.reports;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import rip.alpha.core.bukkit.data.server.GlobalServerData;
import rip.alpha.core.bukkit.data.server.GlobalServerDataManager;
import rip.alpha.core.shared.server.NetworkServerHandler;
import rip.alpha.libraries.gui.Button;
import rip.alpha.libraries.gui.PaginatedMenu;
import rip.alpha.libraries.util.item.ItemBuilder;
import rip.alpha.libraries.util.message.MessageBuilder;
import rip.alpha.libraries.util.task.TaskUtil;

import java.util.ArrayList;
import java.util.List;

public class ReportMenu extends PaginatedMenu {

    public static void openFor(Player player) {
        List<Report> reportList = new ArrayList<>();
        player.sendMessage(MessageBuilder.construct("Fetching data..."));
        NetworkServerHandler.getInstance().getServersAsync().thenAcceptAsync(networkServers -> networkServers.forEach(networkServer -> {
            GlobalServerData serverData = GlobalServerDataManager.servers().getOrCreateRealTimeData(networkServer.getServerId());
            ReportData reportData = serverData.getReportData();
            reportList.addAll(reportData.getOpenReports());
        })).thenAccept(unused -> TaskUtil.runSync(() -> new ReportMenu(reportList).open(player)));
    }

    private final List<Report> reportList;
    private final String currentServer;

    private ReportMenu(List<Report> reportList) {
        super("&6Reports", 4 * 9);
        this.reportList = reportList;
        this.currentServer = NetworkServerHandler.getInstance().getCurrentServer().getServerId();
    }

    @Override
    protected void setupPaginatedMenu(HumanEntity humanEntity) {
        this.reportList.stream().map(this::getReportButton).forEach(this::addPageElement);
    }

    private Button getReportButton(Report report) {
        return Button.builder()
                .itemCreator(player -> {
                    ItemBuilder builder = new ItemBuilder(Material.PAPER)
                            .name("§cReport by §f" + report.reporterName())
                            .lore(" ")
                            .lore("§6Target: §f" + report.reportedName())
                            .lore("§6Reason: §f" + report.reportReason())
                            .lore("§6Server: §f" + report.serverID())
                            .lore("§6Location: §f" + report.reportLocation())
                            .lore(" ")
                            .lore("§7[Left -> Teleport]")
                            .lore("§7[Right -> Remove]");
                    if (this.currentServer.equals(report.serverID())) {
                        builder.enchantment(Enchantment.ARROW_DAMAGE);
                        builder.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    }
                    return builder.build();
                })
                .eventConsumer(event -> {
                    Player player = (Player) event.getWhoClicked();
                    if (event.isRightClick()) {
                        player.closeInventory();
                        player.sendMessage(MessageBuilder.construct("Removing report..."));
                        GlobalServerDataManager.servers()
                                .applyToDataAsync(report.serverID(), data -> data.getReportData().removeReport(report))
                                .thenRun(() -> openFor(player));
                    } else {
                        if (this.currentServer.equals(report.serverID())) {
                            player.teleport(report.reportLocation().toBukkit());
                            player.sendMessage(MessageBuilder.construct("Teleporting to {}.", report.reportLocation()));
                        } else {
                            player.sendMessage(MessageBuilder.constructError("Not on the same server."));
                        }
                    }
                }).build();
    }
}
