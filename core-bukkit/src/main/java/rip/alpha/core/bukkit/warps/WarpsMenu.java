package rip.alpha.core.bukkit.warps;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import rip.alpha.core.bukkit.data.server.LocalServerDataManager;
import rip.alpha.libraries.gui.Button;
import rip.alpha.libraries.gui.PaginatedMenu;
import rip.alpha.libraries.util.item.ItemBuilder;
import rip.alpha.libraries.util.message.MessageBuilder;

public class WarpsMenu extends PaginatedMenu {

    private final WarpData warpData;

    public WarpsMenu() {
        super("&6Warps", 4 * 9);
        this.warpData = LocalServerDataManager.getCurrent().getWarpData();
    }

    @Override
    protected void setupPaginatedMenu(HumanEntity humanEntity) {
        this.warpData.getAllWarps().stream().map(this::getWarpButton).forEach(this::addPageElement);
    }

    private Button getWarpButton(Warp warp) {
        return Button.builder()
                .itemCreator(player -> new ItemBuilder(Material.GLASS)
                        .name("§6" + warp.name())
                        .lore(" ")
                        .lore("§7[Left -> Teleport]")
                        .lore("§7[Right -> Remove]")
                        .build())
                .eventConsumer(event -> {
                    Player player = (Player) event.getWhoClicked();
                    if (event.isRightClick()) {
                        this.warpData.removeWarp(warp.name());
                        player.sendMessage(MessageBuilder.construct("You removed {}.", warp.name()));
                        this.open(player);
                    } else {
                        player.teleport(warp.location());
                        player.sendMessage(MessageBuilder.construct("You teleported to {}.", warp.name()));
                    }
                }).build();
    }
}
