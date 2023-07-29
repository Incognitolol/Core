package rip.alpha.core.shared.server;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NetworkServerType {

    HUB(true),
    RESTRICTED_HUB(true),
    BANNED_HUB(true),
    PRODUCTION(false),
    DEVELOPMENT(false),
    BUILD(false),
    OTHER(false);

    private final boolean hub;

}
