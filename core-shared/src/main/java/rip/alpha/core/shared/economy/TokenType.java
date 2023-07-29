package rip.alpha.core.shared.economy;

import lombok.AllArgsConstructor;
import lombok.Getter;
import rip.alpha.libraries.util.message.MessageColor;

@Getter
@AllArgsConstructor
public enum TokenType {

    BOUGHT("Alpha Coin", "Alpha Coins", "⛃", MessageColor.GOLD.toString()),
    EARNED("Alpha Credit", "Alpha Credits", "❖", MessageColor.BLUE.toString());

    private final String displaySingular, displayPlural, symbol, color;

}
