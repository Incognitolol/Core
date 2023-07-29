package rip.alpha.core.shared.tests;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class ImmutableData {

    private final String name;
    private final int age;
    private final UUID uid;

}
