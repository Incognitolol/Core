package rip.alpha.core.bukkit.modsuite.reports;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ReportData {

    private final Set<Report> openReports = new LinkedHashSet<>();

    public List<Report> getOpenReports() {
        return List.copyOf(this.openReports);
    }

    public void removeReport(Report report) {
        this.openReports.remove(report);
    }

    public void addReport(Report report) {
        this.openReports.add(report);
    }
}
