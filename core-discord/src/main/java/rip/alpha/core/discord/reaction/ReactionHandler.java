package rip.alpha.core.discord.reaction;

import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import rip.alpha.core.discord.CoreBot;
import rip.alpha.core.discord.reaction.data.ReactionRole;
import rip.alpha.core.discord.reaction.listener.ReactionListener;
import rip.alpha.libraries.Libraries;
import rip.alpha.libraries.model.DataManager;
import rip.alpha.libraries.model.DomainContext;
import rip.alpha.libraries.model.LocalDataDomain;

import java.util.UUID;

/**
 * @author Moose1301
 * @date 4/13/2022
 */
public class ReactionHandler {
   @Getter
   private final LocalDataDomain<UUID, ReactionRole> reactionRoles;
    public ReactionHandler(JDA jda) {
        MongoDatabase database = Libraries.getMongoClient().getDatabase("corebot");
        DomainContext<UUID, ReactionRole> reactionRoleContext = DomainContext.<UUID, ReactionRole>builder()
                .creator(ReactionRole::new)
                .keyClass(UUID.class)
                .keyFunction(ReactionRole::getId)
                .mongoDatabase(database)
                .namespace("reaction-roles")
                .valueClass(ReactionRole.class)
                .build();
        this.reactionRoles = DataManager.getInstance().getOrCreateLocalDomain(reactionRoleContext);
        this.reactionRoles.loadAllValuesIntoLocalCache();
        jda.addEventListener(new ReactionListener(CoreBot.getInstance().getJda()));

    }
    public ReactionRole getReactionRoleByMessage(long messageID) {
        return reactionRoles.findInLocalCache(reactionRole -> reactionRole.getMessageId() == messageID).orElse(null);
    }

}
