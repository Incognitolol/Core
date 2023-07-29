package rip.alpha.core.bukkit.essentials;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import rip.alpha.libraries.command.annotation.Command;
import rip.alpha.libraries.command.annotation.CommandUsage;
import rip.alpha.libraries.command.annotation.Default;
import rip.alpha.libraries.spawner.SpawnerEntry;
import rip.alpha.libraries.util.EntityUtils;

public class SpawnerCommand {

    @Command(names = {"spawner give"}, permission = "op")
    @CommandUsage("<type> <player>")
    public static void spawner(CommandSender sender, EntityType type, @Default("self") Player target) {
        SpawnerEntry entry = new SpawnerEntry(type);
        ItemStack itemStack = entry.toItemStack();
        target.getInventory().addItem(itemStack);
        sender.sendMessage(ChatColor.GREEN + "You have given that player a mob spawner");
    }

    @Command(names = {"spawner type"}, permission = "op")
    @CommandUsage("<type>")
    public static void spawnerType(Player player) {
        Block block = player.getTargetBlock(null, 5);
        BlockState state = block.getState();

        if (!(state instanceof CreatureSpawner)) {
            player.sendMessage(ChatColor.RED + "You need to be looking at a spawner");
            return;
        }

        CreatureSpawner creatureSpawner = (CreatureSpawner) state;
        player.sendMessage(ChatColor.GREEN + "That is a " + EntityUtils.getName(creatureSpawner.getSpawnedType()) + " spawner.");
    }
}
