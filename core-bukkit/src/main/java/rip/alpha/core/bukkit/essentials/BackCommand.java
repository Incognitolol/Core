package rip.alpha.core.bukkit.essentials;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import rip.alpha.core.bukkit.data.player.CorePlayerProfileManager;
import rip.alpha.libraries.command.annotation.Command;
import rip.alpha.libraries.command.annotation.CommandUsage;
import rip.alpha.libraries.command.annotation.Default;
import rip.alpha.libraries.util.message.MessageBuilder;
import rip.foxtrot.spigot.util.Hoverable;

public class BackCommand {
    @CommandUsage("(amount)")
    @Command(names = {"back"}, permission = "core.command.back")
    public static void onCommand(Player player, @Default("1") int place) {
        if (place < 0 || place > 5) {
            player.sendMessage(MessageBuilder.constructError("You need a value between 1-5."));
            return;
        }

        int index = place - 1;
        Location location = CorePlayerProfileManager.profiles()
                .getData(player.getUniqueId())
                .getLocationTrack()
                .getTrackedLocation(index);

        if (location == null) {
            player.sendMessage(MessageBuilder.constructError("There is no location to jump to."));
        } else {
            player.teleport(location);
            String msg = MessageBuilder.standard("You jumped to [ {}, {}, {} in {} ]")
                    .element(location.getBlockX())
                    .element(location.getBlockY())
                    .element(location.getBlockZ())
                    .element(location.getWorld().getName())
                    .build();
            player.sendMessage(msg);
        }
    }

    @CommandUsage("(target)")
    @Command(names = {"backtrack"}, permission = "core.command.backtrack")
    public static void onCommandTrack(Player player, @Default("self") Player target) {
        if (!target.equals(player) && !player.hasPermission("core.command.backtrack.other")) {
            player.sendMessage(MessageBuilder.constructError("You dont have the permissions to see other peoples backtrack."));
            return;
        }
        player.sendMessage(MessageBuilder.construct("Backtrack of {}", target.getName()));
        CorePlayerProfileManager.profiles()
                .getData(target.getUniqueId())
                .getLocationTrack()
                .getAllLocations()
                .forEach(location -> {
                    String msg = MessageBuilder.standard("[ {}, {}, {} in {} ]")
                            .element(location.getBlockX())
                            .element(location.getBlockY())
                            .element(location.getBlockZ())
                            .element(location.getWorld().getName())
                            .build();
                    Hoverable hoverable = new Hoverable(msg);
                    hoverable.send(player);
                });

    }

}
