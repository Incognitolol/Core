package rip.alpha.core.bukkit.essentials;

import org.bukkit.command.CommandSender;
import rip.alpha.core.shared.data.AlphaProfileManager;
import rip.alpha.core.shared.server.NetworkServerEntity;
import rip.alpha.libraries.command.annotation.Command;
import rip.alpha.libraries.command.annotation.CommandUsage;
import rip.alpha.libraries.util.message.MessageBuilder;

public class FindCommand {
    @CommandUsage("<player>")
    @Command(names = {"find"}, async = true, permission = "core.command.find")
    public static void findCommand(CommandSender sender, NetworkServerEntity entity) {
        AlphaProfileManager.profiles().getOrCreateRealTimeDataAsync(entity.entityId()).thenAccept(alphaProfile -> {
            String message = MessageBuilder.construct("{} is currently on {}.", alphaProfile.getLastSeenName(), alphaProfile.getLastVisitedServer());
            sender.sendMessage(message);
        });
    }
}
