package rip.alpha.core.bukkit.economy;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import rip.alpha.core.shared.economy.EconomyEcosystem;
import rip.alpha.core.shared.economy.EconomyManager;
import rip.alpha.core.shared.economy.EconomyResponse;
import rip.alpha.core.shared.economy.TokenType;
import rip.alpha.libraries.command.annotation.Command;
import rip.alpha.libraries.command.annotation.CommandDescription;
import rip.alpha.libraries.command.annotation.CommandUsage;
import rip.alpha.libraries.command.annotation.Default;
import rip.alpha.libraries.util.data.NameCache;
import rip.alpha.libraries.util.message.MessageBuilder;
import rip.alpha.libraries.util.message.MessageConstants;

import java.util.UUID;

public class EconomyCommands {
    @Command(names = {"economy circulation", "eco circulation"}, async = true, permission = "op")
    public static void onCirculation(CommandSender sender) {
        sender.sendMessage(MessageBuilder.construct("Global token circulation:"));
        for (TokenType type : TokenType.values()) {
            String amount = EconomyEcosystem.getCirculation(type).toString();
            sender.sendMessage(MessageBuilder.construct(" - {}: {}", type.getDisplayPlural(), amount));
        }
    }

    @CommandUsage("<target>")
    @Command(names = {"economy", "eco", "tokens", "coins"}, async = true)
    public static void onEconomy(CommandSender sender, @Default("self") UUID targetID) {
        if (sender instanceof Player player && !player.getUniqueId().equals(targetID)) {
            if (!player.hasPermission("core.command.economy.other")) {
                player.sendMessage(MessageBuilder.constructError("You dont have the permission to see the balance of other players."));
                return;
            }
            NameCache.getInstance().getNameAsync(targetID).thenAccept(playerName -> {
                sender.sendMessage(MessageBuilder.construct("Tokens of {}:", playerName));
                for (TokenType type : TokenType.values()) {
                    String amount = EconomyManager.getFunds(targetID, type).toString();
                    sender.sendMessage(MessageBuilder.construct(" - {}: {}", (type.getColor() + type.getDisplayPlural()), type.getSymbol() + amount));
                }
            });
            return;
        }
        sender.sendMessage(MessageBuilder.construct("Your tokens:"));
        for (TokenType type : TokenType.values()) {
            String amount = EconomyManager.getFunds(targetID, type).toString();
            sender.sendMessage(MessageBuilder.construct(" - {}: {}", (type.getColor() + type.getDisplayPlural()), type.getSymbol() + amount));
        }
    }

    @CommandDescription("Used to grant somebody a specified amount of tokens")
    @CommandUsage("<target> <bought|earned> <amount>")
    @Command(names = {"ecoadmin add"}, async = true, permission = "op")
    public static void ecoAdminAdd(CommandSender sender, UUID targetUUID, TokenType type, int amount) {
        EconomyResponse response = EconomyManager.addToFunds(sender.getName(), targetUUID, type, amount);
        NameCache.getInstance().getNameAsync(targetUUID).thenAccept(targetName -> {
            if (response == EconomyResponse.SUCCESS) {
                sender.sendMessage(MessageBuilder.construct("You have successfully given {} {} {} tokens.", targetName, amount, type.name()));
            } else if (response == EconomyResponse.EXCEPTION) {
                sender.sendMessage(MessageBuilder.construct("Something went wrong while add tokens to {}.", targetName));
            }
        });
    }

    @CommandDescription("Used to take away tokens from somebody")
    @CommandUsage("<target> <bought|earned> <amount>")
    @Command(names = {"ecoadmin remove"}, async = true, permission = "op")
    public static void ecoAdminRemove(CommandSender sender, UUID targetUUID, TokenType type, int amount) {
        EconomyResponse response = EconomyManager.removeFromFunds(sender.getName(), targetUUID, type, amount);
        NameCache.getInstance().getNameAsync(targetUUID).thenAccept(targetName -> {
            if (response == EconomyResponse.SUCCESS) {
                sender.sendMessage(MessageBuilder.construct("You have successfully taken {} {} {} tokens.", targetName, amount, type.name()));
            } else if (response == EconomyResponse.LOW_FUNDS) {
                sender.sendMessage(MessageBuilder.construct("{} does not have enough funds to take away.", targetName));
            } else if (response == EconomyResponse.EXCEPTION) {
                String name = MessageConstants.completeWithSOrApostrophe(targetName);
                sender.sendMessage(MessageBuilder.construct("Something went wrong while taking away {} tokens.", name));
            }
        });
    }
}
