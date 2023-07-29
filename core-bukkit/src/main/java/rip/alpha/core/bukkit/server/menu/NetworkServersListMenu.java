package rip.alpha.core.bukkit.server.menu;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import rip.alpha.core.shared.server.NetworkServer;
import rip.alpha.core.shared.server.NetworkServerPlatform;
import rip.alpha.core.shared.server.NetworkServerStatus;
import rip.alpha.libraries.chatinput.ChatInput;
import rip.alpha.libraries.gui.Button;
import rip.alpha.libraries.gui.ConfirmationMenu;
import rip.alpha.libraries.gui.PaginatedMenu;
import rip.alpha.libraries.util.BungeeUtil;
import rip.alpha.libraries.util.TimeUtil;
import rip.alpha.libraries.util.item.ItemBuilder;
import rip.alpha.libraries.util.message.MessageBuilder;
import rip.alpha.libraries.util.message.MessageColor;
import rip.alpha.libraries.util.message.MessageTranslator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class NetworkServersListMenu extends PaginatedMenu {

    private final Collection<NetworkServer> networkServers;

    public NetworkServersListMenu(Collection<NetworkServer> networkServers) {
        super("&6Servers", 18);
        this.networkServers = networkServers;
    }

    @Override
    protected void setupPaginatedMenu(HumanEntity humanEntity) {
        this.networkServers.stream()
                .sorted(Comparator.comparingInt(o -> o.getServerPlatform().ordinal()))
                .map(this::getButton)
                .forEach(this::addPageElement);
    }

    private Button getButton(NetworkServer networkServer) {
        return Button.builder()
                .itemCreator(player -> this.getItemStack((Player) player, networkServer))
                .eventConsumer(event -> this.onClick(event, networkServer))
                .build();
    }

    private ItemStack getItemStack(Player player, NetworkServer networkServer) {
        Material material = Material.COAL;
        switch (networkServer.getServerPlatform()) {
            case BUKKIT -> material = Material.IRON_INGOT;
            case VELOCITY -> material = Material.GOLD_INGOT;
        }
        return new ItemBuilder(material)
                .name(MessageColor.LIGHT_PURPLE + networkServer.getServerId())
                .lore(this.getLore(player, networkServer))
                .build();
    }

    private List<String> getLore(Player player, NetworkServer networkServer) {
        return MessageTranslator.translateLines(new ArrayList<>() {{
            this.add("");

            NetworkServerPlatform platform = networkServer.getServerPlatform();
            this.add("&ePlatform: &d" + platform);
            this.add("&eType: &d" + networkServer.getServerType().name());

            NetworkServerStatus status = networkServer.getNetworkServerStatus();
            if (status != null) {
                this.add("&eStatus: &d" + status.name());
            }

            if (platform != NetworkServerPlatform.OTHER) {
                this.add("&eOnline Players: &d" + networkServer.getConnectedEntities().size());
            }

            if (platform == NetworkServerPlatform.BUKKIT) {
                this.add("&eMax Players: &d" + networkServer.getMetadataValue("maxPlayers", int.class));
                this.add("&ePort: &d" + networkServer.getMetadataValue("serverPort", int.class));
            }

            if (networkServer.hasMetadataValue("shutdownTime")) {
                this.add("");
                this.add("&cThis server is rebooting in " + TimeUtil.formatIntoMMSS(networkServer.getMetadataValue("shutdownTime", int.class)));
            }

            if (player.hasPermission("core.networkserver.shutdown")) {
                this.add("");
                this.add("&7[Left -> Join]");
                this.add("&7[Right -> Shutdown]");
            }

            this.add("");
        }});
    }

    private void onClick(InventoryClickEvent event, NetworkServer server) {
        if (event.isLeftClick() && server.getServerPlatform() == NetworkServerPlatform.BUKKIT) {
            BungeeUtil.sendToServer((Player) event.getWhoClicked(), server.getServerId());
        } else if (event.isRightClick()) {
            new ConfirmationMenu("Are you sure?", shouldShutDown -> {
                if (shouldShutDown) {
                    this.askForDuration((Player) event.getWhoClicked(), server);
                }
                event.getWhoClicked().closeInventory();
            }).open(event.getWhoClicked());
        }
    }

    private void askForDuration(Player player, NetworkServer server) {
        ChatInput.request(player, "What do you want the servers shutdown time to be?", List.of("5s", "5m", "15", "30m"), duration -> {
            Long time = TimeUtil.parseTime(duration);

            if (time == null || time <= -1) {
                player.sendMessage(MessageBuilder.constructError("That is an invalid duration, please try again."));
                this.askForDuration(player, server);
                return;
            }

            server.shutdown((int) (time / 1000L));
        });
    }
}
