package rip.alpha.core.shared.tests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimpleData {

    private String name;
    private int age;
    private UUID uid;

}
