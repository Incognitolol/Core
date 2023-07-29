package rip.alpha.core.bukkit.essentials;

import org.bukkit.command.CommandSender;
import rip.alpha.core.shared.data.AlphaProfileUtilities;
import rip.alpha.core.shared.ranks.Rank;
import rip.alpha.core.shared.server.NetworkServer;
import rip.alpha.core.shared.server.NetworkServerEntity;
import rip.alpha.core.shared.server.NetworkServerHandler;
import rip.alpha.core.shared.server.NetworkServerPlatform;
import rip.alpha.libraries.command.annotation.Command;
import rip.alpha.libraries.util.data.NameCache;
import rip.alpha.libraries.util.message.MessageBuilder;
import rip.alpha.libraries.util.message.MessageColor;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.ExecutionException;

public class GListCommand {
    @Command(names = {"glist", "globallist"}, permission = "core.command.glist", async = true)
    public static void glistCommand(CommandSender sender) {
        NetworkServerHandler.getInstance().getServersAsync().thenAccept(networkServers -> {
            try {
                List<String> messages = new ArrayList<>();
                int counter = 0;
                for (NetworkServer networkServer : networkServers) {
                    if (networkServer.getConnectedEntities().isEmpty()) {
                        continue;
                    }

                    if (networkServer.getServerPlatform() != NetworkServerPlatform.BUKKIT) {
                        continue;
                    }

                    int playerCount = networkServer.getConnectedEntities().size();
                    List<PlayerEntry> playerEntries = new ArrayList<>();
                    counter += playerCount;


                    for (NetworkServerEntity entity : networkServer.getConnectedEntities()) {
                        Rank rank = AlphaProfileUtilities.getRank(entity.entityId()).get();
                        String playerName = NameCache.getInstance().getNameAsync(entity.entityId()).get();
                        playerEntries.add(new PlayerEntry(rank.getColor() + playerName, rank.getPriority()));
                    }
                    playerEntries.sort(PlayerEntry::compareTo);

                    StringJoiner joiner = new StringJoiner(MessageColor.YELLOW + ", ");
                    playerEntries.forEach(playerEntry -> joiner.add(playerEntry.formattedName()));

                    String message = new MessageBuilder(MessageColor.GOLD, MessageColor.WHITE, MessageColor.YELLOW, MessageColor.GOLD)
                            .message("({}) [{}]")
                            .prefix(networkServer.getDisplayName())
                            .element(playerCount)
                            .element(joiner.toString())
                            .build();
                    messages.add(message);
                }

                messages.add(MessageBuilder.construct("There is a total of {} player(s) online", counter));
                messages.forEach(sender::sendMessage);
            } catch (ExecutionException | InterruptedException e) {
                sender.sendMessage(MessageBuilder.constructError("There was an error that occurred while executing this command."));
                e.printStackTrace();
            }
        });
    }

    private record PlayerEntry(String formattedName, int priority) implements Comparable<PlayerEntry> {
        @Override
        public int compareTo(PlayerEntry o) {
            return Integer.compare(-o.priority(), -this.priority);
        }
    }
}
