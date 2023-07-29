package rip.alpha.core.shared.reboot;

import lombok.AllArgsConstructor;
import lombok.Getter;
import rip.alpha.core.shared.server.NetworkServerHandler;

@Getter
@AllArgsConstructor
public class RebootTask implements Runnable {

    private int seconds;
    private boolean autoRestart;

    @Override
    public void run() {
        try {
            if (this.seconds <= 0) {
                RebootHandler.getInstance().cancelRebootTask();
            }

            RebootHandler.getInstance().getRebootStateChangeConsumers().forEach(listener -> listener.onStateChange(this.seconds--));

            if (this.seconds <= 0) {
                return;
            }

            NetworkServerHandler.getInstance().getCurrentServer().setMetadataValue("shutdownTime", this.seconds);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}
