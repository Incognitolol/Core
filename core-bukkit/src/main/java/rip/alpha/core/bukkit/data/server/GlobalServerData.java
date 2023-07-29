package rip.alpha.core.bukkit.data.server;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import rip.alpha.core.bukkit.modsuite.reports.ReportData;
import rip.alpha.core.bukkit.modsuite.requests.RequestsData;

@RequiredArgsConstructor
public class GlobalServerData {

    @Getter
    private final String serverName;
    @Getter
    private final RequestsData requestsData = new RequestsData();
    @Getter
    private final ReportData reportData = new ReportData();

}
