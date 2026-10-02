package project.dao.auth;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import project.firebase.FirebaseConfig;

import java.util.HashMap;
import java.util.Map;

public class RegisterDAO {

    // =====================================================
    // CHECK ACCOUNT EXISTS
    // =====================================================

    public boolean accountExists(
            String email,
            String role
    ) throws Exception {

        Firestore firestore =
                FirebaseConfig.getFirestore();

        String normalizedEmail =
                email.trim().toLowerCase();

        String[] collections =
                {"users", "mechanics", "admins"};

        for (String collection : collections) {

            if (firestore.collection(collection)
                    .document(normalizedEmail)
                    .get()
                    .get()
                    .exists()) {

                return true;
            }

            ApiFuture<QuerySnapshot> future =
                    firestore
                            .collection(collection)
                            .whereEqualTo(
                                    "email",
                                    normalizedEmail
                            )
                            .limit(1)
                            .get();

            QuerySnapshot result =
                    future.get();

            if (!result.isEmpty()) {

                return true;
            }
        }

        return false;
    }


    // =====================================================
    // CREATE ACCOUNT
    // =====================================================

    public void createAccount(
            String name,
            String email,
            String phone,
            String role,
            String password
    ) throws Exception {

        Firestore firestore =
                FirebaseConfig.getFirestore();

        String normalizedEmail =
                email.trim().toLowerCase();

        String collection =
                role.equalsIgnoreCase("Mechanic")
                        ? "mechanics"
                        : "users";


        Map<String, Object> accountData =
                new HashMap<>();

        accountData.put(
                "name",
                name.trim()
        );

        accountData.put(
                "email",
                normalizedEmail
        );

        accountData.put(
                "phone",
                phone.trim()
        );

        accountData.put(
                "password",
                password
        );

        accountData.put(
                "role",
                role
        );

        // -------------------------------------------------
        // CUSTOMER DEFAULT PROFILE
        // -------------------------------------------------

        if (!role.equalsIgnoreCase("Mechanic")) {

            accountData.put(
                    "customerId",
                    normalizedEmail
            );

            accountData.put(
                    "address",
                    ""
            );

            accountData.put(
                    "city",
                    ""
            );

            accountData.put(
                    "profileImageUrl",
                    ""
            );

            accountData.put(
                    "status",
                    "Active"
            );

            accountData.put(
                    "createdAt",
                    String.valueOf(
                            System.currentTimeMillis()
                    )
            );
        }


        // -------------------------------------------------
        // MECHANIC DEFAULT PROFILE
        // -------------------------------------------------

        if (role.equalsIgnoreCase("Mechanic")) {

            accountData.put(
                    "mechanicId",
                    normalizedEmail
            );

            accountData.put(
                    "specialization",
                    ""
            );

            accountData.put(
                    "experience",
                    0
            );

            accountData.put(
                    "status",
                    "Active"
            );

            accountData.put(
                    "profileCompleted",
                    false
            );
        }


        firestore
                .collection(collection)
                .document(normalizedEmail)
                .set(accountData)
                .get();
    }
}
