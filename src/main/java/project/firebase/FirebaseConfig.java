package project.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public final class FirebaseConfig {

    // =====================================================
    // FIRESTORE INSTANCE
    // =====================================================

    private static Firestore firestore;

    // =====================================================
    // FIREBASE CREDENTIAL FILE NAME
    // =====================================================

    private static final String SERVICE_ACCOUNT_FILE =
            "guardian.json";

    // =====================================================
    // PRIVATE CONSTRUCTOR
    // =====================================================

    private FirebaseConfig() {
    }

    // =====================================================
    // GET FIRESTORE
    // =====================================================

    public static synchronized Firestore getFirestore()
            throws IOException {

        /*
         * Firebase आधी initialize झालेला असेल तर
         * पुन्हा initialize करू नका.
         */
        if (firestore != null) {
            return firestore;
        }

        // =================================================
        // INITIALIZE FIREBASE ONLY ONCE
        // =================================================

        if (FirebaseApp.getApps().isEmpty()) {

            GoogleCredentials credentials =
                    loadCredentials();

            FirebaseOptions options =
                    FirebaseOptions
                            .builder()
                            .setCredentials(
                                    credentials
                            )
                            .build();

            FirebaseApp.initializeApp(
                    options
            );

            System.out.println(
                    "Firebase initialized successfully."
            );
        }

        // =================================================
        // GET FIRESTORE INSTANCE
        // =================================================

        firestore =
                FirestoreClient
                        .getFirestore();

        if (firestore == null) {

            throw new IOException(
                    "Unable to initialize Firestore."
            );
        }

        return firestore;
    }

    // =====================================================
    // LOAD FIREBASE CREDENTIALS
    // =====================================================

    private static GoogleCredentials loadCredentials()
            throws IOException {

        /*
         * -------------------------------------------------
         * OPTION 1
         * GOOGLE_APPLICATION_CREDENTIALS
         * -------------------------------------------------
         *
         * Future मध्ये guardian.json code/project मध्ये
         * ठेवायची नसेल तर environment variable वापरता येईल.
         */

        String environmentPath =
                System.getenv(
                        "GOOGLE_APPLICATION_CREDENTIALS"
                );

        if (environmentPath != null
                && !environmentPath.isBlank()) {

            File environmentFile =
                    new File(
                            environmentPath
                    );

            if (environmentFile.exists()
                    && environmentFile.isFile()) {

                try (InputStream inputStream =
                             new FileInputStream(
                                     environmentFile
                             )) {

                    System.out.println(
                            "Firebase credentials loaded "
                                    + "from environment variable."
                    );

                    return GoogleCredentials
                            .fromStream(
                                    inputStream
                            );
                }
            }
        }

        /*
         * -------------------------------------------------
         * OPTION 2
         * guardian.json in project root
         * -------------------------------------------------
         *
         * Example:
         *
         * RoadGuardian/
         * ├── guardian.json
         * ├── pom.xml
         * └── src/
         */

        File projectRootFile =
                new File(
                        SERVICE_ACCOUNT_FILE
                );

        if (projectRootFile.exists()
                && projectRootFile.isFile()) {

            try (InputStream inputStream =
                         new FileInputStream(
                                 projectRootFile
                         )) {

                System.out.println(
                        "Firebase credentials loaded "
                                + "from project root."
                );

                return GoogleCredentials
                        .fromStream(
                                inputStream
                        );
            }
        }

        /*
         * -------------------------------------------------
         * OPTION 3
         * Current RoadGuardian project location
         * -------------------------------------------------
         *
         * src/main/java/project/guardian.json
         *
         * File.separator वापरल्यामुळे:
         *
         * Windows ✅
         * Linux   ✅
         * macOS   ✅
         */

        String legacyPath =
                "src"
                        + File.separator
                        + "main"
                        + File.separator
                        + "java"
                        + File.separator
                        + "project"
                        + File.separator
                        + SERVICE_ACCOUNT_FILE;

        File legacyFile =
                new File(
                        legacyPath
                );

        if (legacyFile.exists()
                && legacyFile.isFile()) {

            try (InputStream inputStream =
                         new FileInputStream(
                                 legacyFile
                         )) {

                System.out.println(
                        "Firebase credentials loaded from: "
                                + legacyPath
                );

                return GoogleCredentials
                        .fromStream(
                                inputStream
                        );
            }
        }

        // =================================================
        // FILE NOT FOUND
        // =================================================

        throw new IOException(
                "Firebase guardian.json file not found. "
                        + "Expected guardian.json in project root "
                        + "or src/main/java/project/guardian.json."
        );
    }

    // =====================================================
    // CHECK FIREBASE INITIALIZATION
    // =====================================================

    public static boolean isInitialized() {

        return firestore != null
                && !FirebaseApp
                .getApps()
                .isEmpty();
    }

    // =====================================================
    // TEST FIRESTORE CONNECTION
    // =====================================================

    public static boolean testConnection() {

        try {

            Firestore db =
                    getFirestore();

            /*
             * users collection मधून फक्त 1 document
             * read करण्याचा lightweight test.
             */

            db.collection("users")
                    .limit(1)
                    .get()
                    .get();

            System.out.println(
                    "Firestore connection successful."
            );

            return true;

        } catch (Exception e) {

            System.err.println(
                    "Firestore connection failed: "
                            + e.getMessage()
            );

            return false;
        }
    }
}