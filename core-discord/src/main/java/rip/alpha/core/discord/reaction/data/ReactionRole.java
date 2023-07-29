package rip.alpha.core.discord.reaction.data;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

/**
 * @author Moose1301
 * @date 4/13/2022
 */
@AllArgsConstructor @Data
public class ReactionRole {
    private final UUID id;
    private long guildId;
    private long channelId;
    private long messageId;
    private Map<String, Long> roles;
    public ReactionRole(UUID id) {
        this.id = id;
    }
}
