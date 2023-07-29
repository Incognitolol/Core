package rip.alpha.core.bukkit.economy.shop;

import org.bukkit.entity.Player;
import rip.alpha.libraries.command.annotation.Command;

public class ShopCommand {
    @Command(names = {"shop", "store"})
    public static void shopCommand(Player player) {
        new ShopCategoryMenu().open(player);
    }
}
