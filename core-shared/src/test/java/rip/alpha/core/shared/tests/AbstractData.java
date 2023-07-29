package rip.alpha.core.shared.tests;

import lombok.AllArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
public abstract class AbstractData {

    private final String name;
    private final UUID uuid;

    public abstract String getID();

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AbstractData other)) {
            return false;
        }
        return other.name.equals(this.name) && other.uuid.equals(this.uuid) && other.getID().equals(this.getID());
    }
}
