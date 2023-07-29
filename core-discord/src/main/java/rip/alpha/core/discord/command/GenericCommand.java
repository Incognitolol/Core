package rip.alpha.core.discord.command;

import lombok.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import rip.alpha.core.discord.command.util.CommandContext;

import java.beans.ConstructorProperties;


@Getter
public abstract class GenericCommand
{
    protected final String name;
    protected final String description;
    protected final boolean publicDiscord;
    protected final boolean staffDiscord;
    protected boolean developer;

    @ConstructorProperties({"Name", "Description", "Appear in Public Discord", "Appear in Staff Discord"})
    public GenericCommand(String name, String description, boolean publicDiscord, boolean staffDiscord) {
        this.name = name.toLowerCase();
        this.description = description;
        this.publicDiscord = publicDiscord;
        this.staffDiscord = staffDiscord;
    }
    @ConstructorProperties({"Name", "Description", "Appear in Public Discord", "Appear in Staff Discord", "Developer Only"})
    public GenericCommand(String name, String description, boolean publicDiscord, boolean staffDiscord, boolean developer) {
        this.name = name.toLowerCase();
        this.description = description + (developer ? "(Developer Only)" : "");
        this.publicDiscord = publicDiscord;
        this.staffDiscord = staffDiscord;
        this.developer = developer;
    }
    public abstract void execute(CommandContext ctx);
    public abstract CommandData register(JDA jda);
}