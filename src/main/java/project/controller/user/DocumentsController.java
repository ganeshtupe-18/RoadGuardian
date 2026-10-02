package project.controller.user;

import project.dao.user.DocumentsDAO;

import java.util.List;
import java.util.Map;

public class DocumentsController {

    private final DocumentsDAO documentsDAO;

    public DocumentsController() {

        DocumentsDAO dao;

        try {
            dao = new DocumentsDAO();

        } catch (Exception e) {
            System.err.println(
                    "DocumentsDAO initialization error: " + e.getMessage()
            );
            e.printStackTrace();
            dao = null;
        }

        this.documentsDAO = dao;
    }

    // ============================================================
    // SAVE DOCUMENT
    // ============================================================

    public boolean saveDocument(
            String userEmail,
            String documentType,
            String fileName,
            String localFilePath
    ) {

        if (documentsDAO == null) {
            return false;
        }

        String email = normalizeEmail(userEmail);

        if (email == null
                || isBlank(documentType)
                || isBlank(fileName)) {
            return false;
        }

        return documentsDAO.saveDocument(
                email,
                documentType.trim(),
                fileName.trim(),
                localFilePath == null ? "" : localFilePath.trim()
        );
    }

    // ============================================================
    // LOAD DOCUMENTS
    // ============================================================

    public List<Map<String, Object>> getDocuments(
            String userEmail
    ) {

        if (documentsDAO == null) {
            return List.of();
        }

        String email = normalizeEmail(userEmail);

        if (email == null) {
            return List.of();
        }

        return documentsDAO.getDocuments(email);
    }

    // ============================================================
    // GET SINGLE DOCUMENT
    // ============================================================

    public Map<String, Object> getDocument(
            String userEmail,
            String documentType
    ) {

        if (documentsDAO == null) {
            return null;
        }

        String email = normalizeEmail(userEmail);

        if (email == null || isBlank(documentType)) {
            return null;
        }

        return documentsDAO.getDocument(
                email,
                documentType.trim()
        );
    }

    // ============================================================
    // DELETE DOCUMENT
    // ============================================================

    public boolean deleteDocument(
            String userEmail,
            String documentType
    ) {

        if (documentsDAO == null) {
            return false;
        }

        String email = normalizeEmail(userEmail);

        if (email == null || isBlank(documentType)) {
            return false;
        }

        return documentsDAO.deleteDocument(
                email,
                documentType.trim()
        );
    }

    // ============================================================
    // HELPERS
    // ============================================================

    private String normalizeEmail(String email) {

        if (email == null) {
            return null;
        }

        String value = email.trim();

        return value.isEmpty()
                ? null
                : value.toLowerCase();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
