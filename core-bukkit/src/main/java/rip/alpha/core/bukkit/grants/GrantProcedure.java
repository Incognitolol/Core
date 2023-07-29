package rip.alpha.core.bukkit.grants;

import lombok.RequiredArgsConstructor;
import net.minecraft.server.v1_7_R4.EnchantmentGlow;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import rip.alpha.core.shared.data.AlphaProfileUtilities;
import rip.alpha.core.shared.ranks.Rank;
import rip.alpha.libraries.chatinput.ChatInput;
import rip.alpha.libraries.gui.Button;
import rip.alpha.libraries.gui.PaginatedMenu;
import rip.alpha.libraries.util.TimeUtil;
import rip.alpha.libraries.util.item.ItemBuilder;
import rip.alpha.libraries.util.message.MessageBuilder;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class GrantProcedure {

    private final UUID targetID;

    private Rank rank = null;
    private String reason = null;
    private long duration = -1;

    public void askForRank(Player player) {
        new SelectRankProcedure(this).open(player);
    }

    private void askForDuration(Player player) {
        ChatInput.request(player, "What is the duration for this grant?", List.of("Permanent", "1y", "90d", "30d"), response -> {
            Long duration = this.parseTime(response);

            if (duration == null) {
                player.sendMessage(MessageBuilder.constructError("{} is an invalid duration.", response));
                this.askForDuration(player);
                return;
            }

            this.duration = duration;
            this.askForReason(player);
        });
    }

    private void askForReason(Player player) {
        ChatInput.request(player, "What is the reason for this grant?", List.of("Promoted", "Purchased"), response -> {
            this.reason = response;
            this.proceedToGrant(player);
        });
    }

    private void proceedToGrant(Player player) {
        AlphaProfileUtilities.addGrant(this.targetID, this.rank, player.getDisguisedName(), this.reason, this.duration).thenRun(() -> {
            player.sendMessage(ChatColor.GREEN + "That grant has been successfully added");
        });
    }

    private Long parseTime(String input) {
        if (input.equalsIgnoreCase("perm") || input.equalsIgnoreCase("permanent")) {
            return -1L;
        }

        return TimeUtil.parseTime(input);
    }

    private static class SelectRankProcedure extends PaginatedMenu {
        private final GrantProcedure grantProcedure;

        public SelectRankProcedure(GrantProcedure grantProcedure) {
            super("Select a rank...", 18);
            this.grantProcedure = grantProcedure;
        }

        @Override
        protected void setupPaginatedMenu(HumanEntity humanEntity) {
            for (Rank rank : Rank.values()) {
                if (rank == Rank.DEFAULT) {
                    continue;
                }
                if (!humanEntity.isOp() && !humanEntity.hasPermission("core.grant." + rank.name().toLowerCase())) {
                    continue;
                }
                this.addPageElement(this.getRankButton(rank));
            }
        }

        private Button getRankButton(Rank rank) {
            return Button.builder()
                    .itemCreator(humanEntity -> this.getRankIcon(rank))
                    .eventConsumer(event -> {
                        event.getWhoClicked().closeInventory();
                        this.grantProcedure.rank = rank;
                        this.grantProcedure.askForDuration((Player) event.getWhoClicked());
                    })
                    .build();
        }

        private ItemStack getRankIcon(Rank rank) {
            java.awt.Color javaColor = rank.getJavaColor();
            int red = javaColor.getRed(), green = javaColor.getGreen(), blue = javaColor.getBlue();
            return new ItemBuilder(Material.LEATHER_CHESTPLATE)
                    .color(Color.fromRGB(red, green, blue))
                    .name(rank.getDisplayName())
                    .enchantment(new EnchantmentGlow(-1))
                    .build();
        }
    }
}
