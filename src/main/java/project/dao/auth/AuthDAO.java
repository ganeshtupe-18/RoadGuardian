package project.dao.auth;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;

import project.firebase.FirebaseConfig;

public class AuthDAO {

    // =====================================================
    // FIND ACCOUNT BY EMAIL
    // =====================================================

    public DocumentSnapshot findAccountByEmail(
            String email
    ) throws Exception {

        Firestore firestore =
                FirebaseConfig.getFirestore();

        String normalizedEmail =
                email == null
                        ? null
                        : email.trim().toLowerCase();

        if (normalizedEmail == null
                || normalizedEmail.isBlank()) {

            return null;
        }

        DocumentSnapshot account;

        // Direct document lookup first. New registration uses
        // normalized email as document ID.
        account = firestore.collection("users")
                .document(normalizedEmail)
                .get()
                .get();

        if (account.exists()) {
            return account;
        }

        account = firestore.collection("mechanics")
                .document(normalizedEmail)
                .get()
                .get();

        if (account.exists()) {
            return account;
        }

        account = firestore.collection("admins")
                .document(normalizedEmail)
                .get()
                .get();

        if (account.exists()) {
            return account;
        }

        // Legacy fallback: random document ID but email field exists.
        account = findByEmailField(firestore, "users", normalizedEmail);
        if (account != null) return account;

        account = findByEmailField(firestore, "mechanics", normalizedEmail);
        if (account != null) return account;

        account = findByEmailField(firestore, "admins", normalizedEmail);
        if (account != null) return account;

        return null;
    }

    // =====================================================
    // CHECK PASSWORD
    // =====================================================

    public boolean checkPassword(
            DocumentSnapshot account,
            String password
    ) {

        if (account == null
                || password == null) {
            return false;
        }

        String storedPassword =
                account.getString("password");

        if (storedPassword == null) {
            return false;
        }

        return storedPassword.equals(password);
    }

    // =====================================================
    // GET ROLE
    // =====================================================

    public String getRole(
            DocumentSnapshot account
    ) {

        if (account == null) {
            return null;
        }

        String role =
                account.getString("role");

        if (role != null
                && !role.trim().isEmpty()) {

            return role.trim();
        }

        String collection =
                account.getReference()
                        .getParent()
                        .getId();

        if ("mechanics".equalsIgnoreCase(collection)) {
            return "Mechanic";
        }

        if ("users".equalsIgnoreCase(collection)) {
            return "User";
        }

        if ("admins".equalsIgnoreCase(collection)) {
            return "Admin";
        }

        return null;
    }

    // =====================================================
    // LEGACY EMAIL FIELD SEARCH
    // =====================================================

    private DocumentSnapshot findByEmailField(
            Firestore firestore,
            String collection,
            String email
    ) throws Exception {

        QuerySnapshot snapshot =
                firestore
                        .collection(collection)
                        .whereEqualTo("email", email)
                        .limit(1)
                        .get()
                        .get();

        if (snapshot.isEmpty()) {
            return null;
        }

        return snapshot.getDocuments().get(0);
    }
}
