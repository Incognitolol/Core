package rip.alpha.core.bukkit.essentials;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import rip.alpha.libraries.command.annotation.Command;
import rip.alpha.libraries.command.annotation.CommandDescription;
import rip.alpha.libraries.command.annotation.CommandUsage;
import rip.alpha.libraries.command.annotation.Wildcard;
import rip.alpha.libraries.util.message.MessageBuilder;
import rip.alpha.libraries.util.message.MessageTranslator;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class LoreCommand {

    @CommandUsage("[loreMessage...]")
    @CommandDescription("Add a lore to your held item")
    @Command(names = {"lore add"}, permission = "core.command.lore")
    public static void loreAddCommand(Player player, @Wildcard String loreMessage) {
        if (!updateLore(player, lore -> addLine(lore, loreMessage))) {
            return;
        }

        String message = MessageBuilder.construct("You have added that lore to the item in your hand.");

        player.sendMessage(message);
    }

    @CommandUsage("<lineNumber>")
    @CommandDescription("Add a lore from your held item")
    @Command(names = {"lore remove"}, permission = "core.command.lore")
    public static void loreRemoveCommand(Player player, int lineNumber) {
        if (!updateLore(player, lore -> removeLine(lore, lineNumber))) {
            return;
        }

        String message = MessageBuilder.construct("You have removed that line in the lore of the item in your hand.");

        player.sendMessage(message);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean updateLore(Player player, Function<List<String>, List<String>> function) {
        ItemStack itemStack = player.getItemInHand();

        if (itemStack == null || itemStack.getType() == Material.AIR) {
            player.sendMessage(MessageBuilder.constructError("You are not holding an item in your hand."));
            return false;
        }

        ItemMeta itemMeta = itemStack.getItemMeta();
        List<String> lore = itemMeta.getLore();

        if (lore == null) {
            lore = new ArrayList<>();
        }

        lore = function.apply(lore);
        itemMeta.setLore(lore);
        player.getItemInHand().setItemMeta(itemMeta);

        return true;
    }

    private static List<String> addLine(List<String> lore, String loreMessage) {
        lore.add(MessageTranslator.translate(loreMessage));
        return lore;
    }

    private static List<String> removeLine(List<String> lore, int index) {
        if ((index < 0 || index >= lore.size())) {
            return lore;
        }
        lore.remove(index);
        return lore;
    }

}
