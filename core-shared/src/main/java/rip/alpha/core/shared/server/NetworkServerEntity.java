package rip.alpha.core.shared.server;

import java.util.Objects;
import java.util.UUID;

public record NetworkServerEntity(UUID entityId, String entityName) {

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NetworkServerEntity entity)) {
            return false;
        }

        return entity.entityId().equals(this.entityId) &&
                entity.entityName().equals(entityName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.entityId, this.entityName);
    }

}
