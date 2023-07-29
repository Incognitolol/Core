package rip.alpha.core.bukkit.essentials;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import rip.alpha.core.shared.AlphaCore;
import rip.alpha.core.shared.data.AlphaProfileManager;
import rip.alpha.core.shared.data.events.ProfileSyncEvent;
import rip.alpha.libraries.command.annotation.Command;

import java.util.Random;

/**
 * @author Moose1301
 * @date 4/10/2022
 */
public class SyncCommand {
    @Command(names = "sync", async = true)
    public static void execute(Player player) {
        AlphaProfileManager.profiles().applyToData(player.getUniqueId(), profile -> {
            if (profile.getDiscordId() != null) {
                player.sendMessage(ChatColor.GREEN + "You have already synced your discord account");
                return;
            }
            String code;
            if (profile.getSyncCode() != null) {
                code = profile.getSyncCode();
            } else {
                profile.setSyncCode(code = getRandomCode());
            }
            AlphaCore.callEvent(new ProfileSyncEvent(code, player.getUniqueId()));

            player.sendMessage(ChatColor.GOLD + "Join our discord and run " + ChatColor.GREEN + "/sync " + code);

        });

    }

    private static String getRandomCode() {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}

