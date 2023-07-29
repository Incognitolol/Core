package rip.alpha.core.bukkit.common;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.bukkit.scheduler.BukkitTask;
import rip.alpha.core.bukkit.Core;
import rip.alpha.core.shared.data.AlphaProfileManager;
import rip.alpha.core.shared.grants.Grant;
import rip.alpha.core.shared.grants.ProfileGrantRemoveEvent;
import rip.alpha.core.shared.punishments.ProfilePunishmentRemoveEvent;
import rip.alpha.core.shared.punishments.Punishment;
import rip.alpha.libraries.logging.LogLevel;
import rip.alpha.libraries.util.task.TaskUtil;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ScheduledProfileTaskTracker {

    private static final Table<UUID, Grant, BukkitTask> grantTasks = HashBasedTable.create();
    private static final Table<UUID, Punishment, BukkitTask> punishmentTasks = HashBasedTable.create();
    private static final Lock grantLock = new ReentrantLock();
    private static final Lock punishmentLock = new ReentrantLock();

    public static void addTask(UUID playerID, Grant grant) {
        if (grant.isInfinite()) {
            return;
        }
        Core.LOGGER.log("Added %s grant task for %s".formatted(grant.rank(), playerID), LogLevel.VERBOSE);
        grantLock.lock();
        int ticks = grant.hasExpired() ? 0 : (int) (grant.getTimeLeft() / 1000 * 20);
        grantTasks.put(playerID, grant, TaskUtil.runTaskLaterAsynchronously(() ->
                AlphaProfileManager.profiles().applyToData(playerID, profile ->
                        profile.removeGrant(grant, ProfileGrantRemoveEvent.Reason.RUN_OUT, "CONSOLE", "Expired")), ticks));
        grantLock.unlock();
    }

    public static void addTask(UUID playerID, Punishment punishment) {
        if (punishment.isInfinite()) {
            return;
        }
        Core.LOGGER.log("Added %s punishment task for %s".formatted(punishment.type(), playerID), LogLevel.VERBOSE);
        punishmentLock.lock();
        int ticks = punishment.hasExpired() ? 0 : (int) (punishment.getTimeLeft() / 1000 * 20);
        punishmentTasks.put(playerID, punishment, TaskUtil.runTaskLaterAsynchronously(() ->
                AlphaProfileManager.profiles().applyToData(playerID, profile ->
                        profile.removePunishment(punishment, ProfilePunishmentRemoveEvent.Reason.RUN_OUT, "CONSOLE", "Expired")), ticks));
        punishmentLock.unlock();
    }

    protected static void terminate(UUID playerID) {
        grantLock.lock();
        Core.LOGGER.log("Terminating grant tasks for %s".formatted(playerID), LogLevel.VERBOSE);
        Map<Grant, BukkitTask> grantMap = grantTasks.rowMap().remove(playerID);
        if (grantMap != null) {
            grantMap.values().forEach(BukkitTask::cancel);
        }
        grantLock.unlock();

        punishmentLock.lock();
        Core.LOGGER.log("Terminating punishment tasks for %s".formatted(playerID), LogLevel.VERBOSE);
        Map<Punishment, BukkitTask> taskMap = punishmentTasks.rowMap().remove(playerID);
        if (taskMap != null) {
            taskMap.values().forEach(BukkitTask::cancel);
        }
        punishmentLock.unlock();

    }

}
