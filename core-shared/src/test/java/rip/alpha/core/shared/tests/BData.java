package rip.alpha.core.shared.tests;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class BData implements IData {

    private final String id;
    private final UUID uid;

    @Override
    public String getID() {
        return this.id;
    }

    @Override
    public UUID getUID() {
        return this.uid;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BData other)) {
            return false;
        }
        return this.getID().equals(other.getID()) && this.getUID().equals(other.getUID());
    }

}
