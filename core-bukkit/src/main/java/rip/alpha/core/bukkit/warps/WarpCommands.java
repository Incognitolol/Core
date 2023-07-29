package rip.alpha.core.bukkit.warps;

import org.bukkit.entity.Player;
import rip.alpha.core.bukkit.data.server.LocalServerDataManager;
import rip.alpha.libraries.command.annotation.Command;
import rip.alpha.libraries.command.annotation.CommandUsage;
import rip.alpha.libraries.util.message.MessageBuilder;

public class WarpCommands {

    @CommandUsage("<name>")
    @Command(names = {"warp create"}, permission = "core.command.warp.create")
    public static void onCreate(Player sender, String name) {
        WarpData data = LocalServerDataManager.getCurrent().getWarpData();
        if (data.containsName(name)) {
            sender.sendMessage(MessageBuilder.constructError("A warp with that name already exists."));
            return;
        }
        data.addWarp(new Warp(name, sender.getLocation()));
        sender.sendMessage(MessageBuilder.construct("You created the warp {}.", name));
    }

    @CommandUsage("<name>")
    @Command(names = {"warp remove"}, permission = "core.command.warp.delete")
    public static void onRemove(Player sender, String name) {
        WarpData data = LocalServerDataManager.getCurrent().getWarpData();
        if (!data.containsName(name)) {
            sender.sendMessage(MessageBuilder.constructError("No warp with that name exists."));
            return;
        }
        data.removeWarp(name);
        sender.sendMessage(MessageBuilder.construct("You deleted the warp {}.", name));
    }

    @CommandUsage("<name>")
    @Command(names = {"warp tp"}, permission = "core.command.warp.tp")
    public static void onTp(Player sender, String name) {
        WarpData data = LocalServerDataManager.getCurrent().getWarpData();
        if (!data.containsName(name)) {
            sender.sendMessage(MessageBuilder.constructError("No warp with that name exists.", name));
            return;
        }
        sender.teleport(data.getWarp(name).location());
        sender.sendMessage(MessageBuilder.construct("You teleported to {}.", name));
    }

    @Command(names = {"warps"}, permission = "core.command.warps")
    public static void onWarps(Player sender) {
        new WarpsMenu().open(sender);
        sender.sendMessage(MessageBuilder.construct("You opened the warps menu."));
    }

}
