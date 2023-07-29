package rip.alpha.core.bukkit.essentials;

import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import rip.alpha.libraries.command.annotation.Command;
import rip.alpha.libraries.command.annotation.CommandDescription;
import rip.alpha.libraries.command.annotation.CommandUsage;
import rip.alpha.libraries.command.annotation.Default;
import rip.alpha.libraries.util.message.MessageBuilder;
import rip.alpha.libraries.util.message.MessageConstants;

public class GamemodeCommand {

    @CommandUsage("(target)")
    @CommandDescription("Set yourself or a target players gamemode to survival")
    @Command(names = {"gms", "gm 0", "gm0", "gamemode 0", "gamemode s"}, permission = "core.command.gms")
    public static void onCommandSurvival(CommandSender sender, @Default("self") Player target) {
        if (!sender.equals(target) && !sender.hasPermission("core.command.gms.other")) {
            String message = MessageBuilder.constructError("You dont have the permission to change the game-mode of others.");
            sender.sendMessage(message);
            return;
        }

        handleGameModeCommand(sender, target, GameMode.SURVIVAL);
    }

    @CommandUsage("(target)")
    @CommandDescription("Set yourself or a target players gamemode to creative")
    @Command(names = {"gmc", "gm 1", "gm1", "gamemode 1", "gamemode c"}, permission = "core.command.gmc")
    public static void onCommandCreative(CommandSender sender, @Default("self") Player target) {
        if (!sender.equals(target) && !sender.hasPermission("core.command.gmc.other")) {
            String message = MessageBuilder.constructError("You dont have the permission to change the game-mode of others.");
            sender.sendMessage(message);
            return;
        }

        handleGameModeCommand(sender, target, GameMode.CREATIVE);
    }

    private static void handleGameModeCommand(CommandSender sender, Player target, GameMode gameMode) {
        target.setGameMode(gameMode);

        String message = MessageBuilder.construct("Your gamemode has been set to {}.", gameMode.name());
        target.sendMessage(message);

        if (!sender.equals(target)) {
            String apName = MessageConstants.completeWithSOrApostrophe(target.getName());
            String senderMessage = MessageBuilder.construct("{} gamemode has been set to {}.", apName, gameMode.name());
            sender.sendMessage(senderMessage);
        }
    }
}
