package rip.alpha.core.shared.data;

import lombok.Getter;
import lombok.Setter;
import rip.alpha.core.shared.grants.Grant;
import rip.alpha.core.shared.grants.ProfileGrantAddEvent;
import rip.alpha.core.shared.grants.ProfileGrantRemoveEvent;
import rip.alpha.core.shared.punishments.ProfilePunishmentAddEvent;
import rip.alpha.core.shared.punishments.ProfilePunishmentRemoveEvent;
import rip.alpha.core.shared.punishments.Punishment;
import rip.alpha.core.shared.ranks.Rank;

import java.util.*;

public class AlphaProfile {

    @Getter
    private final UUID mojangID;

    @Getter
    private final GrantHistory grantHistory;
    private final Map<Rank, List<Grant>> grants;

    @Getter
    private final PunishmentHistory punishmentHistory;
    private final Map<Punishment.Type, Punishment> activePunishments;

    @Getter
    private final AlphaProfileSettings profileSettings = new AlphaProfileSettings();

    @Getter
    @Setter
    private boolean joinedBefore, currentlyOnline;

    @Getter
    @Setter
    private long lastJoin, firstJoin;

    @Getter
    @Setter
    private String lastSeenName, lastVisitedServer;

    @Getter
    @Setter
    private String lastActiveIp, lastAuthenticatedIp, totpSecret, syncCode, discordId;

    @Getter
    @Setter
    private int voteStreak, votesToday;

    @Getter
    @Setter
    private long voteStreakTimeOut, voteTimeOut;

    protected AlphaProfile() {
        this(null);
    }

    protected AlphaProfile(UUID mojangID) {
        this.mojangID = mojangID;
        this.grants = new EnumMap<>(Rank.class);
        this.grantHistory = new GrantHistory();
        this.punishmentHistory = new PunishmentHistory();
        this.activePunishments = new EnumMap<>(Punishment.Type.class);
        this.lastActiveIp = null;
        this.lastAuthenticatedIp = null;
        this.totpSecret = null;
        this.joinedBefore = false;
        this.currentlyOnline = false;
        this.firstJoin = -1;
        this.lastSeenName = null;
        this.lastVisitedServer = null;
        this.syncCode = null;
        this.discordId = null;
        this.voteStreak = 0;
        this.voteStreakTimeOut = -1;

        for (Rank rank : Rank.values()) {
            this.grants.put(rank, new ArrayList<>());
        }
    }

    public Rank getHighestRank() {
        return this.getGrantedRanks().stream().min(Comparator.comparingInt(Rank::getPriority)).orElse(Rank.DEFAULT);
    }

    public List<Rank> getGrantedRanks() {
        return this.getAllGrants().stream().map(Grant::rank).toList();
    }

    public List<Grant> getAllGrants() {
        return this.grants.values().stream().flatMap(List::stream).toList();
    }

    public void addGrant(Grant grant) {
        this.grants.computeIfAbsent(grant.rank(), rank -> new ArrayList<>()).add(grant);
        new ProfileGrantAddEvent(this.mojangID, grant, !this.hasRank(grant.rank())).callEvent();
    }

    public boolean removeGrant(Grant grant, ProfileGrantRemoveEvent.Reason reason, String removedBy, String removedReason) {
        boolean removed = this.grants.computeIfAbsent(grant.rank(), rank -> new ArrayList<>()).removeIf(found -> found.equals(grant));

        if (!removed) {
            return false;
        }

        new ProfileGrantRemoveEvent(this.mojangID, grant, reason, !this.hasRank(grant.rank()), removedBy, removedReason).callEvent();
        this.grantHistory.add(grant, removedBy, removedReason);
        return true;
    }

    public boolean hasRank(Rank rank) {
        return !this.getGrantsForRank(rank).isEmpty();
    }

    public List<Grant> getGrantsForRank(Rank rank) {
        return List.copyOf(this.grants.computeIfAbsent(rank, key -> new ArrayList<>()));
    }

    public List<Punishment> getAllActivePunishments() {
        return List.copyOf(this.activePunishments.values());
    }

    public Punishment getActivePunishment(Punishment.Type type) {
        return this.activePunishments.get(type);
    }


    public boolean hasPunishment(Punishment.Type type) {
        return this.activePunishments.containsKey(type);
    }

    public void addPunishment(Punishment punishment) {
        this.removePunishmentForType(punishment.type(), ProfilePunishmentRemoveEvent.Reason.ADDED_ANOTHER, "CONSOLE", "Another was added");

        if (punishment.type() == Punishment.Type.SERVER_WARN) {
            this.punishmentHistory.add(punishment, "CONSOLE", "Warn");
        } else {
            this.activePunishments.put(punishment.type(), punishment);
        }

        new ProfilePunishmentAddEvent(this.mojangID, punishment).callEvent();
    }

    public boolean removePunishment(Punishment punishment, ProfilePunishmentRemoveEvent.Reason reason, String removedBy, String removeReason) {
        Punishment removed = this.activePunishments.remove(punishment.type());

        if (removed == null) {
            return false;
        }

        this.punishmentHistory.add(removed, removedBy, removeReason);
        new ProfilePunishmentRemoveEvent(this.mojangID, removed, reason, removedBy, removeReason).callEvent();
        return true;
    }

    public boolean removePunishmentForType(Punishment.Type type, ProfilePunishmentRemoveEvent.Reason reason, String removedBy, String removedReason) {
        Punishment punishment = this.getActivePunishment(type);
        if (punishment == null) {
            return false;
        }
        return this.removePunishment(punishment, reason, removedBy, removedReason);
    }

    public int removeGrants(Rank rank, ProfileGrantRemoveEvent.Reason taken, String removedBy, String reason) {
        int counter = 0;
        for (Grant grant : this.getGrantsForRank(rank)) {
            if (this.removeGrant(grant, taken, removedBy, reason)) {
                counter++;
            }
        }
        return counter;
    }

}