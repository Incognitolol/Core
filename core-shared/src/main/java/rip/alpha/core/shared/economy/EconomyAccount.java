package rip.alpha.core.shared.economy;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class EconomyAccount {

    @Getter
    private final UUID ownerID;
    private final Map<TokenType, BigInteger> tokenMap = new HashMap<>();

    protected BigInteger getAmount(TokenType tokenType) {
        return this.tokenMap.computeIfAbsent(tokenType, key -> BigInteger.ZERO);
    }

    protected boolean has(TokenType type, BigInteger amount) {
        return this.getAmount(type).compareTo(amount) >= 0;
    }

    protected void add(TokenType type, BigInteger amount) {
        BigInteger newAmount = this.getAmount(type).add(amount);
        this.tokenMap.put(type, newAmount);
    }

    protected void remove(TokenType type, BigInteger amount) {
        BigInteger newAmount = this.getAmount(type).subtract(amount);
        if (newAmount.compareTo(BigInteger.ZERO) < 0) {
            throw new IllegalEconomyException("Tried to remove more money than present.");
        }
        this.tokenMap.put(type, newAmount);
    }

}
