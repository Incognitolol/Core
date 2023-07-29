package rip.alpha.core.bukkit.essentials;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import rip.alpha.libraries.command.annotation.Command;
import rip.alpha.libraries.command.annotation.CommandDescription;
import rip.alpha.libraries.command.annotation.CommandUsage;
import rip.alpha.libraries.command.annotation.Default;
import rip.alpha.libraries.util.message.MessageBuilder;
import rip.alpha.libraries.util.message.MessageConstants;

public class HeadCommand {

    @CommandUsage("(target)")
    @CommandDescription("Get somebodies skull")
    @Command(names = {"head", "skull"}, permission = "core.command.head")
    public static void onCommand(Player player, @Default("self") Player target) {
        ItemStack itemStack = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
        meta.setOwner(target.getName());
        itemStack.setItemMeta(meta);
        player.getInventory().addItem(itemStack);

        String apName = MessageConstants.completeWithSOrApostrophe(target.getName());
        String message = MessageBuilder.construct("You have been given {} head.", apName);

        player.sendMessage(message);
    }

}
