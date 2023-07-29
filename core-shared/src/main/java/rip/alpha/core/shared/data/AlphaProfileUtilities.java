package rip.alpha.core.shared.data;

import rip.alpha.core.shared.grants.Grant;
import rip.alpha.core.shared.ranks.Rank;
import rip.alpha.libraries.model.GlobalDataDomain;
import rip.alpha.libraries.util.data.NameCache;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class AlphaProfileUtilities {
    public static CompletableFuture<String> getColoredName(UUID playerId) {
        if (playerId == null) {
            return CompletableFuture.completedFuture(null);
        }

        GlobalDataDomain<UUID, AlphaProfile> profiles = AlphaProfileManager.profiles();
        CompletableFuture<String> nameFuture = NameCache.getInstance().getNameAsync(playerId);

        if (profiles.isLocallyCached(playerId)) {
            return CompletableFuture.supplyAsync(() -> profiles.getCachedValue(playerId))
                    .thenCombine(nameFuture, (profile, foundName) -> profile.getHighestRank().getColor() + foundName);
        }

        return profiles.getOrCreateRealTimeDataAsync(playerId)
                .thenCombine(nameFuture, (profile, foundName) -> profile.getHighestRank().getColor() + foundName);
    }

    public static CompletableFuture<String> getColoredName(String name) {
        return NameCache.getInstance().getIDAsync(name).thenCompose(AlphaProfileUtilities::getColoredName);
    }

    public static CompletableFuture<Rank> getRank(UUID playerId) {
        if (playerId == null) {
            return CompletableFuture.completedFuture(null);
        }

        GlobalDataDomain<UUID, AlphaProfile> profiles = AlphaProfileManager.profiles();

        if (profiles.isLocallyCached(playerId)) {
            return CompletableFuture.supplyAsync(() -> profiles.getCachedValue(playerId).getHighestRank());
        }

        return profiles.getOrCreateRealTimeDataAsync(playerId).thenApply(AlphaProfile::getHighestRank);
    }

    public static CompletableFuture<Rank> getRank(String name) {
        return NameCache.getInstance().getIDAsync(name).thenCompose(AlphaProfileUtilities::getRank);
    }

    public static CompletableFuture<Void> addGrant(UUID targetID, Rank rank, String sender, String reason, long duration) {
        return AlphaProfileManager.profiles().applyToDataAsync(targetID, profile -> profile.addGrant(new Grant(rank, sender, reason, duration)));
    }
}
