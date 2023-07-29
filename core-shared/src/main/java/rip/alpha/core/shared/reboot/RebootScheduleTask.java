package rip.alpha.core.shared.reboot;

import rip.alpha.core.shared.server.NetworkServerHandler;
import rip.alpha.core.shared.server.NetworkServerPlatform;
import rip.alpha.core.shared.server.NetworkServerType;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

public class RebootScheduleTask implements Runnable {
    @Override
    public void run() {
        try {
            if (RebootHandler.getInstance().getRebootTask() != null) {
                return;
            }

            LocalDateTime now = LocalDateTime.now();
            NetworkServerPlatform platform = NetworkServerHandler.getInstance().getCurrentServer().getServerPlatform();

            if (platform == NetworkServerPlatform.BUKKIT) {
                boolean aHub = NetworkServerHandler.getInstance().getCurrentServer().getServerType().isHub();

                if (!(now.getHour() == 0 && now.getMinute() == (aHub ? 0 : 30))) {
                    return;
                }

                RebootHandler.getInstance().startReboot((int) TimeUnit.MINUTES.toSeconds(aHub ? 5 : 15), true);
            } else if (platform == NetworkServerPlatform.VELOCITY) {
                if (!(now.getHour() == 1 && now.getMinute() == 0)) {
                    return;
                }

                RebootHandler.getInstance().startReboot((int) TimeUnit.MINUTES.toSeconds(15), true);
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}
