package rip.alpha.core.shared.economy;

import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import rip.alpha.core.shared.AlphaCore;

import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class EconomyEcosystem {

    private static final EconomyEcosystem instance = new EconomyEcosystem();

    private final Map<TokenType, RBucket<BigInteger>> circulationMap = new ConcurrentHashMap<>();
    private final Map<TokenType, RLock> typeLockMap = new ConcurrentHashMap<>();

    public static BigInteger getCirculation(TokenType type) {
        BigInteger value = getBucket(type).get();
        return value == null ? BigInteger.ZERO : value;
    }

    private static RBucket<BigInteger> getBucket(TokenType type) {
        String key = "circulating-" + type;
        return instance.circulationMap.computeIfAbsent(type, k -> AlphaCore.getRedissonClient().getBucket(key));
    }

    private static RLock getLock(TokenType type) {
        String key = "circulating-lock-" + type;
        return instance.typeLockMap.computeIfAbsent(type, k -> AlphaCore.getRedissonClient().getLock(key));
    }

    private static void applyToBucket(TokenType type, Consumer<RBucket<BigInteger>> consumer) {
        RLock lock = getLock(type);
        RBucket<BigInteger> bucket = getBucket(type);
        lock.lock(30, TimeUnit.SECONDS);
        consumer.accept(bucket);
        lock.unlockAsync();
    }

    protected static void circulationAdded(TokenType type, BigInteger amount) {
        applyToBucket(type, bucket -> {
            BigInteger current = bucket.get();
            current = current == null ? BigInteger.ZERO : current;
            bucket.set(current.add(amount));
        });
    }

    protected static void circulationRemoved(TokenType type, BigInteger amount) {
        applyToBucket(type, bucket -> {
            BigInteger current = bucket.get();
            current = current == null ? BigInteger.ZERO : current;
            bucket.set(current.subtract(amount));
        });
    }

}
