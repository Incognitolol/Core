package rip.alpha.core.bukkit.modsuite.requests;

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

public class RequestMenu extends PaginatedMenu {

    public static void openFor(Player player) {
        List<Request> requestList = new ArrayList<>();
        player.sendMessage(MessageBuilder.construct("Fetching data..."));
        NetworkServerHandler.getInstance().getServersAsync().thenAcceptAsync(networkServers -> networkServers.forEach(networkServer -> {
            GlobalServerData serverData = GlobalServerDataManager.servers().getOrCreateRealTimeData(networkServer.getServerId());
            RequestsData requestsData = serverData.getRequestsData();
            requestList.addAll(requestsData.getOpenRequests());
        })).thenAccept(unused -> TaskUtil.runSync(() -> new RequestMenu(requestList).open(player)));
    }

    private final List<Request> requestList;
    private final String currentServer;

    private RequestMenu(List<Request> requestList) {
        super("&6Requests", 4 * 9);
        this.requestList = requestList;
        this.currentServer = NetworkServerHandler.getInstance().getCurrentServer().getServerId();
    }

    @Override
    protected void setupPaginatedMenu(HumanEntity player) {
        this.requestList.stream().map(this::getRequestButton).forEach(this::addPageElement);
    }

    private Button getRequestButton(Request request) {
        return Button.builder()
                .itemCreator(player -> {
                    ItemBuilder builder = new ItemBuilder(Material.PAPER)
                            .name("§cRequested by §f" + request.requesterName())
                            .lore(" ")
                            .lore("§6Message: §f" + request.requestMessage())
                            .lore("§6Server: §f" + request.serverID())
                            .lore("§6Location: §f" + request.requestLocation())
                            .lore(" ")
                            .lore("§7[Left -> Teleport]")
                            .lore("§7[Right -> Remove]");
                    if (this.currentServer.equals(request.serverID())) {
                        builder.enchantment(Enchantment.ARROW_DAMAGE);
                        builder.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    }
                    return builder.build();
                })
                .eventConsumer(event -> {
                    Player player = (Player) event.getWhoClicked();
                    if (event.isRightClick()) {
                        player.closeInventory();
                        player.sendMessage(MessageBuilder.construct("Removing request..."));
                        GlobalServerDataManager.servers()
                                .applyToDataAsync(request.serverID(), data -> data.getRequestsData().removeRequest(request))
                                .thenRun(() -> openFor(player));
                    } else {
                        if (this.currentServer.equals(request.serverID())) {
                            player.teleport(request.requestLocation().toBukkit());
                            player.sendMessage(MessageBuilder.construct("Teleporting to {}.", request.requesterName()));
                        } else {
                            player.sendMessage(MessageBuilder.constructError("Not on the same server."));
                        }
                    }
                }).build();
    }

}
