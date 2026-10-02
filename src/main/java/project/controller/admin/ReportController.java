package project.controller.admin;

import com.google.cloud.firestore.Firestore;
import project.dao.admin.ReportDAO;
import project.model.Report;
import project.ui.user.UserSession;

import java.util.ArrayList;
import java.util.List;

public class ReportController {

    private final ReportDAO reportDAO;

    public ReportController(Firestore firestore) {
        this.reportDAO = new ReportDAO(firestore);
    }

    public List<Report> getAllReports() {
        try {
            List<Report> reports = reportDAO.getAllReports();
            return reports == null ? new ArrayList<>() : reports;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public Report getReportById(String reportId) {
        try {
            return reportDAO.getReportById(reportId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Report generateReport(String name, String type) {
        try {
            String generatedBy = firstNonBlank(
                    UserSession.getUserName(),
                    UserSession.getUserEmail(),
                    "Admin"
            );

            return reportDAO.generateReport(
                    name,
                    type,
                    generatedBy,
                    UserSession.getUserEmail()
            );
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean deleteReport(String reportId) {
        try {
            return reportDAO.deleteReport(reportId);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private String firstNonBlank(String... values) {
        if (values == null) return null;
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return null;
    }
}
