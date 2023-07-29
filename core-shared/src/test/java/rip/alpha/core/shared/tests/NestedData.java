package rip.alpha.core.shared.tests;

import lombok.AllArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
public class NestedData {

    private final Nest nest;
    private final UUID delegateID;

    public record Nest(String name, int age, UUID uid) {
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Nest otherRecord)) {
                return false;
            }
            return otherRecord.age == this.age && otherRecord.name.equals(this.name) && otherRecord.uid.equals(this.uid);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof NestedData otherRecord)) {
            return false;
        }
        return otherRecord.delegateID.equals(this.delegateID) && otherRecord.nest.equals(this.nest);
    }

}
