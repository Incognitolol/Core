package rip.alpha.core.bukkit.grants;

import net.minecraft.server.v1_7_R4.EnchantmentGlow;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import rip.alpha.core.bukkit.Core;
import rip.alpha.core.shared.data.PermissionDataManager;
import rip.alpha.core.shared.ranks.Rank;
import rip.alpha.libraries.chatinput.ChatInput;
import rip.alpha.libraries.gui.Button;
import rip.alpha.libraries.gui.PaginatedMenu;
import rip.alpha.libraries.logging.LogLevel;
import rip.alpha.libraries.util.item.ItemBuilder;
import rip.alpha.libraries.util.message.MessageBuilder;
import rip.alpha.libraries.util.task.TaskUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class RankPermissionMenu extends PaginatedMenu {

    public static void openFor(Player player, Rank rank) {
        player.sendMessage(MessageBuilder.construct("Loading..."));
        Map<Rank, Collection<String>> permMap = new LinkedHashMap<>();
        permMap.put(rank, new ArrayList<>());
        for (Rank cRank : rank.getChildRanks(true)) {
            permMap.put(cRank, new ArrayList<>());
        }
        TaskUtil.runTaskAsynchronously(() -> {
            PermissionDataManager.permissions().applyToAll(permMap.keySet(), data -> {
                permMap.put(data.getRank(), data.getPermissions());
            });
            TaskUtil.runSync(() -> new RankPermissionMenu(rank, permMap).open(player));
        });
    }

    private final Rank rank;
    private final Map<Rank, Collection<String>> permissions;

    private RankPermissionMenu(Rank rank, Map<Rank, Collection<String>> permissions) {
        super(getDisplayName(rank), 4 * 9);
        this.rank = rank;
        this.permissions = permissions;
    }
    public static String getDisplayName(Rank rank) {
        String name = "Permissions: " + rank.getDisplayName();
        if(name.length() >= 28) {
            name = name.substring(0, 20) + "..";
        }
        return name;
    }
    @Override
    protected void setupPaginatedMenu(HumanEntity humanEntity) {
        this.permissions.forEach((keyRank, permissions) -> {
            for (String permission : permissions) {
                this.addPageElement(this.getPermButton(permission, keyRank));
            }
        });
        this.setButton(ButtonPosition.of(6, 4), this.getAddButton());
    }

    private Button getAddButton() {
        return Button.builder()
                .itemCreator(human -> new ItemBuilder(Material.WOOL)
                        .woolColor(DyeColor.GREEN)
                        .name("§aAdd permission")
                        .woolColor(DyeColor.GREEN)
                        .build())
                .eventConsumer(event -> {
                    Player player = (Player) event.getWhoClicked();
                    player.closeInventory();
                    String question = MessageBuilder.construct("Please enter a permission");
                    ChatInput.request(player, question, input -> {
                        player.sendMessage(MessageBuilder.construct("Adding {} to {}...", input, this.rank.getDisplayName()));
                        PermissionDataManager.permissions()
                                .applyToDataAsync(this.rank, data -> data.addPermission(input))
                                .thenRun(() -> TaskUtil.runSync(() -> RankPermissionMenu.openFor(player, this.rank)));
                    });
                }).build();
    }

    private Button getPermButton(String permission, Rank rank) {
        boolean fromChild = rank != this.rank;
        return Button.builder().itemCreator(player -> {
            ItemBuilder builder = new ItemBuilder(Material.PAPER);
            String color = fromChild ? "§f" : "§e";
            builder.name(color + permission);
            if (fromChild) {
                builder.lore(" ");
                builder.lore("§7Inherited from " + rank.getDisplayName());
            } else {
                builder.enchantment(new EnchantmentGlow(0));
                builder.lore(" ");
                builder.lore("§7Right click to remove");
            }
            return builder.build();
        }).eventConsumer(event -> {
            if (fromChild) {
                return;
            }
            if (event.isRightClick()) {
                Player player = (Player) event.getWhoClicked();
                player.closeInventory();
                PermissionDataManager.permissions().applyToDataAsync(this.rank, data -> {
                    player.sendMessage(MessageBuilder.construct("Removing permission..."));
                    data.removePermission(permission);
                }).thenRun(() -> TaskUtil.runSync(() -> openFor(player, rank)));
            }
        }).build();
    }

}
