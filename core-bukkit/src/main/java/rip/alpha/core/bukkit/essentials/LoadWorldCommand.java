package rip.alpha.core.bukkit.essentials;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import rip.alpha.libraries.command.annotation.Command;
import rip.alpha.libraries.command.annotation.CommandUsage;
import rip.alpha.libraries.util.message.MessageBuilder;

import java.util.List;
import java.util.Random;

public class LoadWorldCommand {
    @CommandUsage("<worldName>")
    @Command(names = {"loadworld"}, permission = "core.command.loadworld")
    public static void loadWorldCommand(Player player, String worldName) {
        World worldOfName = Bukkit.getWorld(worldName);

        if (worldOfName != null) {
            player.sendMessage(MessageBuilder.constructError("That world is already loaded"));
            return;
        }

        WorldCreator worldCreator = new WorldCreator(worldName);
        worldCreator.generateStructures(false);
        worldCreator.generator(VoidWorldGenerator.instance);

        World world = Bukkit.createWorld(worldCreator);
        world.setThundering(false);
        world.setStorm(false);
        world.setWeatherDuration(Integer.MAX_VALUE);
        world.setGameRuleValue("doFireTick", "false");
        world.setGameRuleValue("mobGriefing", "false");
        world.setGameRuleValue("doMobSpawning", "false");
        world.setGameRuleValue("doWeatherCycle", "false");

        player.sendMessage(MessageBuilder.construct("{} has been loaded.", world.getName()));
    }

    private static class VoidWorldGenerator extends ChunkGenerator {
        public static VoidWorldGenerator instance = new VoidWorldGenerator();

        private final byte[] bytes = new byte[32768];
        private final List<BlockPopulator> populators = List.of();

        public List<BlockPopulator> getDefaultPopulators(World world) {
            return populators;
        }

        public boolean canSpawn(World world, int x, int z) {
            return true;
        }

        public byte[] generate(World world, Random rand, int chunkx, int chunkz) {
            return bytes;
        }

        public Location getFixedSpawnLocation(World world, Random random) {
            return new Location(world, 0, 80, 0);
        }

        public static VoidWorldGenerator get() {
            return instance;
        }
    }
}
