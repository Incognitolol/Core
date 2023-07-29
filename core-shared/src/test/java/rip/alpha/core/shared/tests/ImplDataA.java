package rip.alpha.core.shared.tests;

import java.util.UUID;

public class ImplDataA extends AbstractData {

    public ImplDataA(String name, UUID uuid) {
        super(name, uuid);
    }

    @Override
    public String getID() {
        return "A";
    }

}
