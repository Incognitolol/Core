package rip.alpha.core.bukkit.essentials;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import rip.alpha.libraries.command.annotation.Command;
import rip.alpha.libraries.command.annotation.CommandUsage;
import rip.alpha.libraries.command.annotation.Wildcard;
import rip.alpha.libraries.util.message.MessageBuilder;
import rip.alpha.libraries.util.message.MessageTranslator;

public class RenameCommand {

    @CommandUsage("[name...]")
    @Command(names = {"rename"}, permission = "core.command.rename")
    public static void renameCommand(Player player, @Wildcard String name) {
        ItemStack itemStack = player.getItemInHand();

        if (itemStack == null || itemStack.getType() == Material.AIR) {
            player.sendMessage(MessageBuilder.constructError("You are not holding an item in your hand."));
            return;
        }

        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(MessageTranslator.translate(name));
        player.getItemInHand().setItemMeta(itemMeta);

        String itemName = ChatColor.stripColor(itemMeta.getDisplayName());
        String message = MessageBuilder.construct("You have renamed the item in your hand to {}.", itemName);

        player.sendMessage(message);
    }

}
