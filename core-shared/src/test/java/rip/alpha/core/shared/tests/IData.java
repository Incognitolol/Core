package rip.alpha.core.shared.tests;

import java.util.UUID;

public interface IData {

    String getID();

    UUID getUID();


    record AData(String id, UUID uid) implements IData {
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
            if (!(obj instanceof AData other)) {
                return false;
            }
            return this.getID().equals(other.getID()) && this.getUID().equals(other.getUID());
        }
    }

}
