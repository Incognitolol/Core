package rip.alpha.core.bukkit.queue;

import org.bukkit.Bukkit;
import rip.alpha.core.bukkit.Core;
import rip.alpha.core.shared.queue.NetworkQueue;
import rip.alpha.core.shared.queue.NetworkQueueSnapshotHandler;
import rip.alpha.core.shared.queue.event.PollQueueEvent;
import rip.alpha.core.shared.reboot.RebootHandler;
import rip.alpha.core.shared.reboot.RebootTask;
import rip.alpha.core.shared.server.NetworkServerHandler;
import rip.alpha.libraries.logging.LogLevel;

public class NetworkQueueRunnable implements Runnable {
    public NetworkQueueRunnable() {
        Core.LOGGER.log("Queue runnable has been started", LogLevel.BASIC);
    }

    @Override
    public void run() {
        try {
            if (NetworkServerHandler.getInstance().getCurrentServer().getServerType().isHub()) {
                return;
            }

            RebootTask rebootTask = RebootHandler.getInstance().getRebootTask();
            if (rebootTask != null && rebootTask.getSeconds() < 120) {
                return;
            }

            if (Bukkit.getOnlinePlayers().size() >= Bukkit.getMaxPlayers()) {
                return;
            }

            if (Bukkit.hasWhitelist()) {
                return;
            }

            String serverId = NetworkServerHandler.getInstance().getCurrentServer().getServerId();
            NetworkQueue.NetworkQueueSnapShot queue = NetworkQueueSnapshotHandler.getInstance().getCachedQueue(serverId);

            if (queue == null || queue.isEmpty()) {
                return;
            }

            new PollQueueEvent(NetworkServerHandler.getInstance().getCurrentServer().getServerId()).callEvent();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}
