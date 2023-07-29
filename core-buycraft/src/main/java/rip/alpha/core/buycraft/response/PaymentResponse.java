package rip.alpha.core.buycraft.response;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author Moose1301
 * @date 11/27/2022
 */
public record PaymentResponse(int id, String amount, String status, String date, Currency currency, Player player, List<Package> packages) {

    public record Player(int id, String name, String uuid){
        public UUID formatUUID() {
            return UUID.fromString(this.uuid
                    .replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"));
        }
    }

    public record Package(int id, String name) {

    }
}
