package rip.alpha.core.shared.reboot;

import lombok.Getter;
import rip.alpha.core.shared.server.NetworkServerHandler;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class RebootHandler {

    @Getter
    private static final RebootHandler instance = new RebootHandler();

    private final Set<RebootStateChangeConsumer> rebootStateChangeConsumers;
    private final ScheduledExecutorService rebootExecutor, scheduledRebootExecutor;
    private ScheduledFuture<?> rebootScheduledFuture;
    private RebootTask rebootTask;

    private RebootHandler() {
        this.rebootExecutor = Executors.newSingleThreadScheduledExecutor();
        this.scheduledRebootExecutor = Executors.newSingleThreadScheduledExecutor();
        this.rebootStateChangeConsumers = new HashSet<>();
        RebootScheduleTask rebootScheduleTask = new RebootScheduleTask();
        this.scheduledRebootExecutor.scheduleAtFixedRate(rebootScheduleTask, 1, 1, TimeUnit.SECONDS);
        this.rebootTask = null;
    }

    public void registerListener(RebootStateChangeConsumer listener) {
        this.rebootStateChangeConsumers.add(listener);
    }

    public void startReboot(int seconds) {
        this.startReboot(seconds, false);
    }

    protected void startReboot(int seconds, boolean autoRestart) {
        if (this.rebootTask != null) {
            throw new IllegalStateException("Attempted to start a reboot task while one was already running");
        }
        this.rebootTask = new RebootTask(seconds, autoRestart);
        this.rebootScheduledFuture = this.rebootExecutor.scheduleAtFixedRate(this.rebootTask, 0, 1, TimeUnit.SECONDS);
    }

    public void cancelRebootTask() {
        NetworkServerHandler.getInstance().getCurrentServer().removeMetadataValue("shutdownTime");
        this.rebootScheduledFuture.cancel(true);
        this.rebootTask = null;
    }

    public RebootTask getRebootTask() {
        return this.rebootTask;
    }

    public void shutdown() {
        this.rebootExecutor.shutdownNow();
        this.scheduledRebootExecutor.shutdownNow();
    }

    protected Collection<RebootStateChangeConsumer> getRebootStateChangeConsumers() {
        return Collections.unmodifiableCollection(this.rebootStateChangeConsumers);
    }
}
