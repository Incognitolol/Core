package rip.alpha.core.bukkit.essentials;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import rip.alpha.core.shared.data.AlphaProfile;
import rip.alpha.core.shared.data.AlphaProfileManager;
import rip.alpha.core.shared.ranks.Rank;
import rip.alpha.libraries.command.annotation.Command;
import rip.alpha.libraries.util.message.MessageColor;
import rip.alpha.libraries.util.message.MessageTranslator;

import java.util.*;

public class ListCommand {
    @Command(names = {"list", "who"}, async = true)
    public static void listCommand(CommandSender commandSender) {
        StringBuilder listBuilder = new StringBuilder();

        List<Rank> ranks = Arrays.asList(Rank.values());
        ranks.sort(Comparator.comparingInt(Rank::getPriority));

        StringJoiner rankBuilder = new StringJoiner(MessageColor.RESET + ", ");
        for (Rank rank : ranks) {
            rankBuilder.add(rank.getDisplayName());
        }

        listBuilder.append(rankBuilder).append("\n");

        List<PlayerListEntry> entries = new ArrayList<>();
        Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        for (Player target : players) {
            if (commandSender instanceof Player player) {
                if (!player.canSee(target)) {
                    continue;
                }
            }
            AlphaProfile snapshot = AlphaProfileManager.profiles().getCachedValue(target.getUniqueId());
            Rank rank = snapshot.getHighestRank();
            entries.add(new PlayerListEntry(rank.getColor() + target.getName(), rank.getPriority()));
        }

        entries.sort(PlayerListEntry::compareTo);
        StringJoiner playerBuilder = new StringJoiner(MessageColor.RESET + ", ", MessageColor.RESET + "[", MessageColor.RESET + "]");
        entries.forEach(playerListEntry -> playerBuilder.add(playerListEntry.playerName()));

        listBuilder.append(MessageTranslator.translate("&f(" + players.size() + "/" + Bukkit.getMaxPlayers() + ")"))
                .append(" ")
                .append(playerBuilder)
                .append("\n");

        commandSender.sendMessage(listBuilder.toString());
    }

    private record PlayerListEntry(String playerName, int priority) implements Comparable<PlayerListEntry> {
        @Override
        public int compareTo(PlayerListEntry o) {
            return Integer.compare(-o.priority(), -this.priority);
        }
    }
}
