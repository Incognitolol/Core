package rip.alpha.core.shared.economy;

import rip.alpha.core.shared.AlphaCore;
import rip.alpha.libraries.model.DataManager;
import rip.alpha.libraries.model.DomainContext;
import rip.alpha.libraries.model.GlobalDataDomain;
import rip.alpha.libraries.model.GlobalDataPocket;

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class EconomyManager {

    private static final EconomyManager instance = new EconomyManager();

    private final GlobalDataDomain<UUID, EconomyAccount> accountDomain;

    private final GlobalDataPocket<EconomyTransaction> transactionHistory;

    private EconomyManager() {
        DomainContext<UUID, EconomyAccount> domainContext = DomainContext.<UUID, EconomyAccount>builder()
                .mongoDatabase(AlphaCore.getDatabase())
                .creator(EconomyAccount::new)
                .valueClass(EconomyAccount.class)
                .namespace("player-economy-accounts")
                .keyClass(UUID.class)
                .redissonClient(AlphaCore.getRedissonClient())
                .keyFunction(EconomyAccount::getOwnerID)
                .build();
        this.accountDomain = DataManager.getInstance().getOrCreateGlobalDomain(domainContext);
        this.transactionHistory = DataManager.getInstance().getOrCreateDataPocket(EconomyTransaction.class);
    }

    public static List<EconomyTransaction> getAllTransactions(UUID ownerID) {
        return instance.transactionHistory.loadAll("targetID", ownerID);
    }

    public static void enableLocalCacheFor(UUID playerID) {
        if (instance.accountDomain.isLocallyCached(playerID)) {
            return;
        }
        instance.accountDomain.enableLocalCacheFor(playerID);
    }

    public static void disableLocalCacheFor(UUID playerID) {
        if (!instance.accountDomain.isLocallyCached(playerID)) {
            return;
        }
        instance.accountDomain.disableLocalCacheFor(playerID);
    }

    public static BigInteger getFunds(UUID ownerID, TokenType type) {
        return instance.accountDomain.getOrCreateRealTimeData(ownerID).getAmount(type);
    }

    public static BigInteger getFundsSnapshot(UUID playerID, TokenType type) {
        return instance.accountDomain.getCachedValue(playerID).getAmount(type);
    }

    public static EconomyResponse addToFunds(String issuer, UUID ownerID, TokenType type, int amount) {
        if (amount < 0) {
            return EconomyResponse.INVALID_PARAMETER;
        }
        AtomicReference<EconomyResponse> response = new AtomicReference<>();
        instance.accountDomain.applyToData(ownerID, data -> {
            try {
                BigInteger addition = BigInteger.valueOf(amount);
                BigInteger before = data.getAmount(type);
                data.add(type, addition);
                BigInteger after = data.getAmount(type);
                long now = System.currentTimeMillis();
                EconomyTransaction transaction = new EconomyTransaction(now, issuer, ownerID, before, after);
                EconomyEcosystem.circulationAdded(type, addition);
                CompletableFuture.runAsync(() -> {
                    instance.transactionHistory.add(transaction);
                    new FundsChangeEvent(ownerID, before, after).callEvent();
                });
                response.set(EconomyResponse.SUCCESS);
            } catch (Exception e) {
                e.printStackTrace();
                response.set(EconomyResponse.EXCEPTION);
            }
        });
        return response.get();
    }

    public static EconomyResponse removeFromFunds(String issuer, UUID ownerID, TokenType type, int amount) {
        if (amount < 0) {
            return EconomyResponse.INVALID_PARAMETER;
        }
        AtomicReference<EconomyResponse> response = new AtomicReference<>();
        instance.accountDomain.applyToData(ownerID, data -> {
            try {
                BigInteger removal = BigInteger.valueOf(amount);
                if (data.has(type, removal)) {
                    BigInteger before = data.getAmount(type);
                    data.remove(type, removal);
                    BigInteger after = data.getAmount(type);
                    long now = System.currentTimeMillis();
                    EconomyTransaction transaction = new EconomyTransaction(now, issuer, ownerID, before, after);
                    EconomyEcosystem.circulationRemoved(type, removal);
                    CompletableFuture.runAsync(() -> {
                        instance.transactionHistory.add(transaction);
                        new FundsChangeEvent(ownerID, before, after).callEvent();
                    });
                    response.set(EconomyResponse.SUCCESS);
                } else {
                    response.set(EconomyResponse.LOW_FUNDS);
                }
            } catch (Exception e) {
                e.printStackTrace();
                response.set(EconomyResponse.EXCEPTION);
            }
        });
        return response.get();
    }

}
