package rip.alpha.core.shared.economy;

import java.math.BigInteger;
import java.util.UUID;

public record EconomyTransaction(
        long timestamp,
        String issuer,
        UUID targetID,
        BigInteger preTransactionValue,
        BigInteger postTransactionValue) {

}
