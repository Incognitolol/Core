package rip.alpha.core.bukkit.grants;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import rip.alpha.core.shared.data.AlphaProfileManager;
import rip.alpha.core.shared.data.AlphaProfileUtilities;
import rip.alpha.core.shared.ranks.Rank;
import rip.alpha.libraries.command.annotation.Command;
import rip.alpha.libraries.command.annotation.CommandUsage;
import rip.alpha.libraries.command.annotation.Default;
import rip.alpha.libraries.command.annotation.Wildcard;
import rip.alpha.libraries.util.message.MessageBuilder;
import rip.alpha.libraries.util.uuid.UUIDFetcher;

import java.time.Duration;
import java.util.UUID;

public class GrantCommands {

    @CommandUsage("<rank>")
    @Command(names = {"permissions"}, async = true, permission = "core.command.permissions")
    public static void grantPermissions(Player player, Rank rank) {
        RankPermissionMenu.openFor(player, rank);
    }

    @CommandUsage("<target>")
    @Command(names = {"grant"}, async = true, permission = "core.command.grant")
    public static void grantAddCommand(Player player, UUID targetID) {
        new GrantProcedure(targetID).askForRank(player);
    }

    @CommandUsage("<target> <rank> <time> [reason...]")
    @Command(names = {"cgrant"}, async = true, permission = "op")
    public static void onCommandGrantAdd(CommandSender sender, UUID targetID, Rank rank, Duration duration, @Wildcard String reason) {
        if (sender instanceof Player) {
            sender.sendMessage(MessageBuilder.constructError("You need to be a console to execute this command."));
            return;
        }

        if (rank == Rank.DEFAULT) {
            sender.sendMessage(MessageBuilder.constructError("You cannot grant the default rank."));
            return;
        }

        AlphaProfileUtilities.addGrant(targetID, rank, sender.getName(), reason, duration.toMillis()).thenRun(() -> {
            sender.sendMessage(ChatColor.GREEN + "You have successfully granted " + rank.getName());
        });
    }

    @CommandUsage("<target>")
    @Command(names = {"grants"}, async = true, permission = "core.command.grants")
    public static void onCommandGrantHistory(Player sender, @Default("self") UUID targetID) {
        if (!AlphaProfileManager.profiles().exists(targetID)) {
            String targetName = UUIDFetcher.getName(targetID);
            sender.sendMessage(MessageBuilder.constructError("Couldn't find a profile with the name '{}'", targetName));
            return;
        }
        GrantsMenu.openFor(sender, targetID);
    }
}
