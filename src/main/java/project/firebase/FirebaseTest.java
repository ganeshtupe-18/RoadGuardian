package project.firebase;

import com.google.cloud.firestore.Firestore;

public class FirebaseTest {

    public static void main(String[] args) {

        try {

            Firestore firestore =
                    FirebaseConfig.getFirestore();

            System.out.println(
                    "Firebase connected successfully!"
            );

            System.out.println(
                    "Firestore connection is ready."
            );

        } catch (Exception e) {

            System.out.println(
                    "Firebase connection failed!"
            );

            e.printStackTrace();
        }
    }
}