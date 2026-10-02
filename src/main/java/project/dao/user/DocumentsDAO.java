package project.dao.user;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.SetOptions;

import project.firebase.FirebaseConfig;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DocumentsDAO {

    private static final String USERS = "users";
    private static final String DOCUMENTS = "documents";

    private final Firestore firestore;

    public DocumentsDAO() throws Exception {
        this.firestore = FirebaseConfig.getFirestore();
    }

    // ============================================================
    // SAVE / UPDATE DOCUMENT METADATA
    // Path stays:
    // users/{email}/documents/{documentTypeId}
    // ============================================================

    public boolean saveDocument(
            String userEmail,
            String documentType,
            String fileName,
            String localFilePath
    ) {

        String email = normalizeEmail(userEmail);
        String type = clean(documentType);
        String name = clean(fileName);
        String path = clean(localFilePath);

        if (email == null || type == null || name == null) {
            return false;
        }

        try {

            String id = documentId(type);

            DocumentSnapshot existing = firestore
                    .collection(USERS)
                    .document(email)
                    .collection(DOCUMENTS)
                    .document(id)
                    .get()
                    .get();

            Map<String, Object> data = new HashMap<>();

            data.put("documentId", id);
            data.put("userId", email);
            data.put("userEmail", email);
            data.put("documentType", type);
            data.put("fileName", name);
            data.put("localFilePath", path == null ? "" : path);
            data.put("status", "Uploaded");
            data.put("updatedAt", System.currentTimeMillis());

            if (!existing.exists()) {
                data.put("createdAt", System.currentTimeMillis());
            }

            firestore
                    .collection(USERS)
                    .document(email)
                    .collection(DOCUMENTS)
                    .document(id)
                    .set(data, SetOptions.merge())
                    .get();

            return true;

        } catch (Exception e) {
            System.err.println(
                    "Document save error: " + e.getMessage()
            );
            e.printStackTrace();
            return false;
        }
    }

    // ============================================================
    // GET ALL DOCUMENTS
    // ============================================================

    public List<Map<String, Object>> getDocuments(String userEmail) {

        List<Map<String, Object>> documents = new ArrayList<>();

        String email = normalizeEmail(userEmail);

        if (email == null) {
            return documents;
        }

        try {

            QuerySnapshot snapshot = firestore
                    .collection(USERS)
                    .document(email)
                    .collection(DOCUMENTS)
                    .get()
                    .get();

            for (DocumentSnapshot document : snapshot.getDocuments()) {

                if (document.getData() == null) {
                    continue;
                }

                Map<String, Object> data = new HashMap<>(document.getData());

                data.put("documentId", document.getId());
                data.put("userId", email);
                data.put("userEmail", email);

                if (isBlank(data.get("status"))) {
                    data.put("status", "Uploaded");
                }

                documents.add(data);
            }

            documents.sort(
                    Comparator.comparing(
                            item -> stringValue(item.get("documentType"))
                                    .toLowerCase()
                    )
            );

        } catch (Exception e) {
            System.err.println(
                    "Document load error: " + e.getMessage()
            );
            e.printStackTrace();
        }

        return documents;
    }

    // ============================================================
    // GET SINGLE DOCUMENT
    // ============================================================

    public Map<String, Object> getDocument(
            String userEmail,
            String documentType
    ) {

        String email = normalizeEmail(userEmail);
        String type = clean(documentType);

        if (email == null || type == null) {
            return null;
        }

        try {

            DocumentSnapshot document = firestore
                    .collection(USERS)
                    .document(email)
                    .collection(DOCUMENTS)
                    .document(documentId(type))
                    .get()
                    .get();

            if (!document.exists() || document.getData() == null) {
                return null;
            }

            Map<String, Object> data = new HashMap<>(document.getData());
            data.put("documentId", document.getId());
            data.put("userId", email);
            data.put("userEmail", email);

            return data;

        } catch (Exception e) {
            System.err.println(
                    "Document read error: " + e.getMessage()
            );
            e.printStackTrace();
            return null;
        }
    }

    // ============================================================
    // DELETE DOCUMENT METADATA
    // ============================================================

    public boolean deleteDocument(
            String userEmail,
            String documentType
    ) {

        String email = normalizeEmail(userEmail);
        String type = clean(documentType);

        if (email == null || type == null) {
            return false;
        }

        try {

            firestore
                    .collection(USERS)
                    .document(email)
                    .collection(DOCUMENTS)
                    .document(documentId(type))
                    .delete()
                    .get();

            return true;

        } catch (Exception e) {
            System.err.println(
                    "Document delete error: " + e.getMessage()
            );
            e.printStackTrace();
            return false;
        }
    }

    // ============================================================
    // HELPERS
    // ============================================================

    private String normalizeEmail(String email) {

        String value = clean(email);

        return value == null
                ? null
                : value.toLowerCase();
    }

    private String documentId(String documentType) {

        String value = clean(documentType);

        if (value == null) {
            return "unknown";
        }

        String id = value
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");

        return id.isEmpty()
                ? "unknown"
                : id;
    }

    private String clean(String value) {

        if (value == null) {
            return null;
        }

        String cleaned = value.trim();

        return cleaned.isEmpty()
                ? null
                : cleaned;
    }

    private boolean isBlank(Object value) {
        return value == null || String.valueOf(value).trim().isEmpty();
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }
}
