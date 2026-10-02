package project.dao.auth;

import com.google.cloud.firestore.Firestore;
import project.firebase.FirebaseConfig;

public class ForgotPasswordDAO {

    public boolean accountExists(String email) throws Exception {

        Firestore firestore = FirebaseConfig.getFirestore();

        return !firestore
                .collection("users")
                .whereEqualTo("email", email.trim().toLowerCase())
                .get()
                .get()
                .isEmpty()
                ||
                !firestore
                .collection("mechanics")
                .whereEqualTo("email", email.trim().toLowerCase())
                .get()
                .get()
                .isEmpty();
    }
}