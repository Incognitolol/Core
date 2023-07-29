package rip.alpha.core.bukkit.totp;

import dev.samstevens.totp.qr.QrData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import rip.alpha.core.shared.data.AlphaProfileManager;
import rip.alpha.libraries.command.annotation.Command;
import rip.alpha.libraries.command.annotation.CommandUsage;
import rip.alpha.libraries.util.message.MessageBuilder;
import rip.alpha.libraries.util.message.MessageConstants;
import rip.alpha.libraries.util.uuid.UUIDFetcher;

import java.util.UUID;

public class TotpCommand {

    @CommandUsage("<code>")
    @Command(names = {"2fa", "auth"}, permission = "core.command.2fa", async = true)
    public static void modSuite2faCommand(Player player, String code) {
        if (!TotpHandler.getInstance().isEnabled()) {
            return;
        }
        if (TotpAuthenticatedCache.getInstance().exists(player)) {
            player.sendMessage(MessageBuilder.constructError("You are already authenticated."));
            return;
        }

        AlphaProfileManager.profiles().applyToData(player.getUniqueId(), profile -> {
            if (profile.getTotpSecret() == null) {
                player.sendMessage(MessageBuilder.constructError("You do not have your 2fa setup, please use /setup2fa"));
                return;
            }

            String secret = profile.getTotpSecret();
            boolean valid = TotpHandler.getInstance().getCodeVerifier().isValidCode(secret, code);

            if (!valid) {
                player.sendMessage(MessageBuilder.constructError("Invalid 2fa code, please try again"));
                return;
            }

            profile.setLastAuthenticatedIp(profile.getLastActiveIp());
            TotpAuthenticatedCache.getInstance().add(player.getUniqueId());
            player.sendMessage(MessageBuilder.construct("You have been successfully authenticated."));
        });
    }

    @Command(names = {"setup2fa", "setupauth"}, permission = "core.command.2fa", async = true)
    public static void setup2faCommand(Player player) {
        if (!TotpHandler.getInstance().isEnabled()) {
            return;
        }
        if (TotpAuthenticatedCache.getInstance().exists(player)) {
            player.sendMessage(MessageBuilder.constructError("You are already authenticated."));
            return;
        }

        AlphaProfileManager.profiles().applyToData(player.getUniqueId(), profile -> {
            if (profile.getTotpSecret() != null) {
                player.sendMessage(MessageBuilder.constructError("You already have a 2fa key setup, if you want this reset, please message a developer"));
                return;
            }

            try {
                player.sendMessage(MessageBuilder.construct("Generating your QR code..."));
                TotpHandler totpHandler = TotpHandler.getInstance();
                String secret = totpHandler.getSecretGenerator().generate();
                QrData qrData = totpHandler.createQRData(player.getName(), secret);
                String imgurURL = totpHandler.uploadQRImage(qrData);
                profile.setTotpSecret(secret);
                player.sendMessage(MessageBuilder.construct("Your QR code can be seen here {}", imgurURL));
                player.sendMessage(MessageBuilder.construct("Once you have registered your 2fa, please use /2fa <code>"));
            } catch (Exception e) {
                player.sendMessage(MessageBuilder.constructError("An error has occurred while trying to create your 2fa info, please message a developer"));
            }
        });
    }

    @CommandUsage("<target>")
    @Command(names = {"reset2fa"}, permission = "core.command.reset2fa", async = true)
    public static void reset2faCommand(CommandSender sender, UUID targetID) {
        if (!TotpHandler.getInstance().isEnabled()) {
            return;
        }
        AlphaProfileManager.profiles().applyToData(targetID, profile -> {
            profile.setLastAuthenticatedIp(null);
            profile.setTotpSecret(null);
            new TotpResetBridgeEvent(targetID).callEvent();
            String name = UUIDFetcher.getName(targetID);
            sender.sendMessage(MessageBuilder.construct("You have reset {} 2fa information!", MessageConstants.completeWithSOrApostrophe(name)));
        });
    }
}
