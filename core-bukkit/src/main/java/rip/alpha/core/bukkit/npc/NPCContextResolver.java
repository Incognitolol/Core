package rip.alpha.core.bukkit.npc;

import rip.alpha.libraries.LibrariesPlugin;
import rip.alpha.libraries.command.context.ContextResolver;
import rip.alpha.libraries.command.context.type.ArgumentContext;
import rip.alpha.libraries.command.context.type.TabCompleteArgumentContext;
import rip.alpha.libraries.fake.FakeEntity;
import rip.alpha.libraries.util.message.MessageBuilder;

import java.util.List;

public class NPCContextResolver implements ContextResolver<NPC> {
    @Override
    public NPC resolve(ArgumentContext<NPC> argumentContext) {
        int id;

        try {
            id = Integer.parseInt(argumentContext.input());
        } catch (NumberFormatException e) {
            argumentContext.sender().sendMessage(MessageBuilder.constructError("That is not a valid id"));
            return null;
        }

        FakeEntity fakeEntity = LibrariesPlugin.getInstance().getFakeEntityHandler().getEntityById(id);

        if (!(fakeEntity instanceof NPC)) {
            argumentContext.sender().sendMessage(MessageBuilder.constructError("That is not an npc"));
            return null;
        }

        return (NPC) fakeEntity;
    }

    @Override
    public List<String> getTabComplete(TabCompleteArgumentContext<NPC> tabCompleteArgumentContext) {
        return null;
    }
}
