package rip.alpha.core.bukkit.essentials;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import rip.alpha.libraries.command.annotation.Command;
import rip.alpha.libraries.command.annotation.CommandUsage;
import rip.alpha.libraries.command.annotation.Default;
import rip.alpha.libraries.util.message.MessageBuilder;
import rip.alpha.libraries.util.message.MessageConstants;

public class EnchantCommand {

    @CommandUsage("<enchantment|DAMAGE_ALL> <level|1> (target)")
    @Command(names = {"enchant"}, permission = "core.command.enchant")
    public static void enchantCommand(CommandSender sender, Enchantment enchantment, @Default("1") int level, @Default("self") Player target) {
        if (!sender.equals(target) && !sender.hasPermission("core.command.enchant.other")) {
            sender.sendMessage(MessageBuilder.constructError("You dont have the permission to enchant for others."));
            return;
        }

        ItemStack itemStack = target.getItemInHand();

        if (itemStack == null || itemStack.getType() == Material.AIR) {
            String message = MessageBuilder.constructError("{} needs to be holding an item in their hand to enchant.", target.getName());
            sender.sendMessage(message);
            return;
        }

        itemStack.addUnsafeEnchantment(enchantment, level);
        target.setItemInHand(itemStack);

        String message = MessageBuilder.standard("The enchantment ({}, {}) has been applied to your held item.")
                .element(enchantment.getName())
                .element(level)
                .build();
        target.sendMessage(message);

        if (!sender.equals(target)) {
            String senderMessage = MessageBuilder.standard("The enchantment ({}, {}) has been applied to {} held item.")
                    .element(enchantment.getName())
                    .element(level)
                    .element(MessageConstants.completeWithSOrApostrophe(target.getName()))
                    .build();
            sender.sendMessage(senderMessage);
        }
    }

}
