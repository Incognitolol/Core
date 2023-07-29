package rip.alpha.core.bukkit.essentials;

import org.bukkit.command.CommandSender;
import rip.alpha.core.bukkit.data.player.CorePlayerProfile;
import rip.alpha.core.bukkit.data.player.CorePlayerProfileManager;
import rip.alpha.core.bukkit.levels.LevelData;
import rip.alpha.core.shared.data.AlphaProfile;
import rip.alpha.core.shared.data.AlphaProfileManager;
import rip.alpha.libraries.command.annotation.Command;
import rip.alpha.libraries.command.annotation.CommandUsage;
import rip.alpha.libraries.util.DateUtil;
import rip.alpha.libraries.util.TimeUtil;
import rip.alpha.libraries.util.message.MessageBuilder;
import rip.alpha.libraries.util.uuid.UUIDFetcher;

import java.sql.Date;
import java.time.Instant;
import java.util.UUID;

public class WhoisCommand {

    @CommandUsage("<target>")
    @Command(names = {"whois"}, permission = "core.command.whois", async = true)
    public static void onCommand(CommandSender sender, UUID targetID) {
        sender.sendMessage(MessageBuilder.construct("Loading data..."));

        if (!AlphaProfileManager.profiles().exists(targetID)) {
            String targetName = UUIDFetcher.getName(targetID);
            sender.sendMessage(MessageBuilder.constructError("Couldn't find a profile with the name '{}'", targetName));
            return;
        }

        CorePlayerProfile coreProfile;
        if (CorePlayerProfileManager.profiles().isDataLoaded(targetID)) {
            coreProfile = CorePlayerProfileManager.profiles().getData(targetID);
        } else {
            coreProfile = CorePlayerProfileManager.profiles().loadAndGetSync(targetID);
            CorePlayerProfileManager.profiles().unloadDataSync(targetID);
        }

        AlphaProfile alphaProfile = AlphaProfileManager.profiles().getOrCreateRealTimeData(targetID);
        sender.sendMessage(getCompiledData(alphaProfile, coreProfile));
    }

    private static String getCompiledData(AlphaProfile alphaProfile, CorePlayerProfile coreProfile) {
        StringBuilder dataBuilder = new StringBuilder();

        LevelData levelData = coreProfile.getLevelData();

        String lastJoin = "Hasn't joined before", firstJoin = "Hasn't joined before", duration = "Hasn't joined before";
        String lastVisitedServer = "Hasn't joined before", lastSeenName = "Hasn't joined before";
        if (alphaProfile.isJoinedBefore()) {
            lastJoin = DateUtil.formatDate(Date.from(Instant.ofEpochMilli(alphaProfile.getLastJoin())));
            firstJoin = DateUtil.formatDate(Date.from(Instant.ofEpochMilli(alphaProfile.getFirstJoin())));
            duration = TimeUtil.formatIntoDetailedString((int) ((System.currentTimeMillis() - alphaProfile.getFirstJoin()) / 1000));
            lastSeenName = alphaProfile.getLastSeenName();
            lastVisitedServer = alphaProfile.getLastVisitedServer();

            if (alphaProfile.isCurrentlyOnline()) {
                lastJoin = "Currently online";
            }
        }

        dataBuilder.append(MessageBuilder.construct("General Data:\n"));
        dataBuilder.append(MessageBuilder.construct("- Current highest rank: {}\n", alphaProfile.getHighestRank().getName()));
        dataBuilder.append(MessageBuilder.construct("- Last seen name: {}\n", lastSeenName));
        dataBuilder.append(MessageBuilder.construct("- Last join: {}\n", lastJoin));
        dataBuilder.append(MessageBuilder.construct("- Last server: {}\n", lastVisitedServer));
        dataBuilder.append(MessageBuilder.construct("- First join: {}\n", firstJoin));
        dataBuilder.append(MessageBuilder.construct("- Time with us: {}\n", duration));

        dataBuilder.append(MessageBuilder.construct("Level Data:\n"));
        dataBuilder.append(MessageBuilder.construct("- Experience: {}\n", levelData.getExperience()));
        dataBuilder.append(MessageBuilder.construct("- Level: {}\n", levelData.getLevel()));

        dataBuilder.append(MessageBuilder.construct("Punishment Data:\n"));
        dataBuilder.append(MessageBuilder.construct("- Active Punishments: {}\n", alphaProfile.getAllActivePunishments().size()));
        dataBuilder.append(MessageBuilder.construct("- Punishment History: {}\n", alphaProfile.getPunishmentHistory().getEntries().size()));
        dataBuilder.append(MessageBuilder.construct("- Last Active IP: {}\n", alphaProfile.getLastActiveIp()));

        dataBuilder.append(MessageBuilder.construct("Grant Data:\n"));
        dataBuilder.append(MessageBuilder.construct("- Active Grants: {}\n", alphaProfile.getAllGrants().size()));
        dataBuilder.append(MessageBuilder.construct("- Grant History: {}\n", alphaProfile.getGrantHistory().getEntries().size()));

        return dataBuilder.toString();
    }

}
