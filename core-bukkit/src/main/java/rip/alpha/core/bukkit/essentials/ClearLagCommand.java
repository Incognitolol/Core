package rip.alpha.core.bukkit.essentials;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import rip.alpha.libraries.command.annotation.Command;
import rip.alpha.libraries.util.message.MessageBuilder;

public class ClearLagCommand {

    @Command(names = {"clearlag"}, permission = "core.command.clearlag")
    public static void clearlagCommand(CommandSender sender) {
        int entitiesRemoved = 0;

        for (World world : Bukkit.getWorlds()) {
            for (Entity itemEntity : world.getEntitiesByClasses(Item.class)) {
                itemEntity.remove();
                entitiesRemoved++;
            }

            for (Entity creatureEntity : world.getEntitiesByClasses(Creature.class)) {
                if (creatureEntity instanceof Tameable tameable) {
                    if (tameable.isTamed()) {
                        continue;
                    }
                }
                creatureEntity.remove();
                entitiesRemoved++;
            }

            for (Entity slimeEntity : world.getEntitiesByClasses(Slime.class)) {
                slimeEntity.remove();
                entitiesRemoved++;
            }
        }

        String message = MessageBuilder.construct("There was a total of {} entity(s) that were removed", entitiesRemoved);
        sender.sendMessage(message);
    }

}
