package rip.alpha.core.bukkit.essentials;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import rip.alpha.libraries.command.annotation.Command;
import rip.alpha.libraries.util.message.MessageBuilder;

public class MoreCommand {

    @Command(names = {"more"}, permission = "core.command.more")
    public static void onCommand(Player player) {
        ItemStack handItem = player.getInventory().getItemInHand();

        if (handItem.getAmount() == 64) {
            player.sendMessage(MessageBuilder.constructError("This is already maxed out."));
        }

        handItem.setAmount(64);
        player.getInventory().setItemInHand(handItem);

        player.sendMessage(MessageBuilder.construct("You have set the item in your hand to {}.", handItem.getAmount()));
    }

}
