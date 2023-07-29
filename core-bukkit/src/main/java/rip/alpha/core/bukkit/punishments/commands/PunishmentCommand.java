package rip.alpha.core.bukkit.punishments.commands;

import org.bukkit.entity.Player;
import rip.alpha.core.bukkit.punishments.PunishmentMenu;
import rip.alpha.core.shared.data.AlphaProfileManager;
import rip.alpha.libraries.command.annotation.Command;
import rip.alpha.libraries.command.annotation.CommandUsage;
import rip.alpha.libraries.command.annotation.Default;
import rip.alpha.libraries.util.message.MessageBuilder;
import rip.alpha.libraries.util.uuid.UUIDFetcher;

import java.util.UUID;

public class PunishmentCommand {
    @CommandUsage("(target)")
    @Command(names = {"punishments", "c", "history", "hist"}, async = true, permission = "core.command.punishments")
    public static void onCommandPunishmentHistory(Player sender, @Default("self") UUID targetID) {
        if (!AlphaProfileManager.profiles().exists(targetID)) {
            String targetName = UUIDFetcher.getName(targetID);
            sender.sendMessage(MessageBuilder.constructError("Couldn't find a profile with the name '{}'", targetName));
            return;
        }

        PunishmentMenu.openFor(sender, targetID);
    }
}
