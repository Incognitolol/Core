package rip.alpha.core.shared.tests;

import java.util.UUID;

public class ImplDataB extends AbstractData {

    public ImplDataB(String name, UUID uuid) {
        super(name, uuid);
    }

    @Override
    public String getID() {
        return "B";
    }

}
