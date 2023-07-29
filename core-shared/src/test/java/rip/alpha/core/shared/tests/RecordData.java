package rip.alpha.core.shared.tests;

import java.util.UUID;

public record RecordData(String name, int age, UUID uid) {
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof RecordData otherRecord)) {
            return false;
        }
        return otherRecord.age == this.age && otherRecord.name.equals(this.name) && otherRecord.uid.equals(this.uid);
    }
}
