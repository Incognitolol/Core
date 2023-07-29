package rip.alpha.core.bukkit.essentials;

import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;
import rip.alpha.libraries.command.annotation.Command;
import rip.alpha.libraries.command.annotation.CommandUsage;
import rip.alpha.libraries.command.annotation.Default;
import rip.alpha.libraries.util.message.MessageBuilder;
import rip.alpha.libraries.util.message.MessageConstants;

public class PingCommand {

    @CommandUsage("(target)")
    @Command(names = {"ping", "ms"})
    public static void pingCommand(CommandSender sender, @Default("self") Player target) {
        if (sender.equals(target)) {
            String message = MessageBuilder.construct("Ping: {}ms", getPing(target));
            sender.sendMessage(message);
            return;
        }

        String apName = MessageConstants.completeWithSOrApostrophe(target.getName());
        String message = MessageBuilder.construct("{} ping: {}ms", apName, getPing(target));
        sender.sendMessage(message);
    }

    private static int getPing(Player player) {
        return ((CraftPlayer) player).getHandle().ping;
    }

}
