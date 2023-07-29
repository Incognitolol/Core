package rip.alpha.core.bukkit.modsuite.requests;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class RequestsData {

    private final Set<Request> openRequests = new LinkedHashSet<>();

    public List<Request> getOpenRequests() {
        return List.copyOf(this.openRequests);
    }

    public void removeRequest(Request request) {
        this.openRequests.remove(request);
    }

    public void addRequest(Request request) {
        this.openRequests.add(request);
    }

}
