package rip.alpha.core.velocity.queue;

import net.kyori.adventure.text.Component;
import rip.alpha.core.shared.queue.NetworkQueue;
import rip.alpha.core.shared.queue.NetworkQueueSnapshotHandler;
import rip.alpha.core.velocity.Core;
import rip.alpha.libraries.util.message.MessageBuilder;

import java.util.List;

public class NetworkQueueMessageRunnable implements Runnable {
    @Override
    public void run() {
        NetworkQueueSnapshotHandler.getInstance().getCachedQueues().forEach(queue -> {
            List<NetworkQueue.NetworkQueuePlayer> players = queue.getPlayers();
            for (int index = 0; index < players.size(); index++) {
                NetworkQueue.NetworkQueuePlayer queuedPlayer = players.get(index);
                this.handleQueueIndex(queue, queuedPlayer, index);
            }
        });
    }

    private void handleQueueIndex(NetworkQueue.NetworkQueueSnapShot queue, NetworkQueue.NetworkQueuePlayer queuedPlayer, int index) {
        final int position = index + 1;
        Core.getInstance().getProxyServer().getPlayer(queuedPlayer.playerId()).ifPresent(player -> {
            String message = MessageBuilder.standard("You are currently in position {} for {}.")
                    .element("#" + position)
                    .element(queue.getServerId())
                    .build();
            player.sendMessage(Component.text(message));
        });
    }
}
