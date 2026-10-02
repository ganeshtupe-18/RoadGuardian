package project.dao.admin;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.SetOptions;
import com.google.cloud.firestore.WriteBatch;

import project.model.Customer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class CustomerDAO {

    // =====================================================
    // COLLECTIONS
    // =====================================================

    private static final String CUSTOMER_COLLECTION =
            "customers";

    private static final String USER_COLLECTION =
            "users";

    private final Firestore firestore;

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public CustomerDAO(
            Firestore firestore
    ) {

        if (firestore == null) {

            throw new IllegalArgumentException(
                    "Firestore cannot be null."
            );
        }

        this.firestore =
                firestore;
    }

    // =====================================================
    // ADD CUSTOMER
    // ADMIN SIDE
    // =====================================================

    public void addCustomer(
            Customer customer
    ) throws ExecutionException,
            InterruptedException {

        if (customer == null) {

            throw new IllegalArgumentException(
                    "Customer cannot be null."
            );
        }

        // -------------------------------------------------
        // CLEAN DATA
        // -------------------------------------------------

        customer.setName(
                cleanOrEmpty(
                        customer.getName()
                )
        );

        customer.setEmail(
                normalizeEmail(
                        customer.getEmail()
                )
        );

        customer.setPhone(
                cleanOrEmpty(
                        customer.getPhone()
                )
        );

        customer.setAddress(
                cleanOrEmpty(
                        customer.getAddress()
                )
        );

        customer.setCity(
                cleanOrEmpty(
                        customer.getCity()
                )
        );

        customer.setProfileImageUrl(
                cleanOrEmpty(
                        customer.getProfileImageUrl()
                )
        );

        if (clean(
                customer.getStatus()
        ) == null) {

            customer.setStatus(
                    "Active"
            );
        }

        if (clean(
                customer.getCreatedAt()
        ) == null) {

            customer.setCreatedAt(
                    currentTime()
            );
        }

        // -------------------------------------------------
        // CHECK IF SAME REGISTERED USER EXISTS
        // -------------------------------------------------

        DocumentSnapshot registeredUser =
                findRegisteredUserByEmail(
                        customer.getEmail()
                );

        /*
         * जर Admin त्याच email चा customer add करत असेल
         * आणि तो user आधी registration मधून users मध्ये
         * आहे, तर त्याचा email/document ID वापरू.
         */
        if (registeredUser != null) {

            customer.setCustomerId(
                    registeredUser.getId()
            );
        }

        // -------------------------------------------------
        // CUSTOMER DOCUMENT
        // -------------------------------------------------

        CollectionReference collection =
                firestore.collection(
                        CUSTOMER_COLLECTION
                );

        DocumentReference document;

        if (clean(
                customer.getCustomerId()
        ) == null) {

            document =
                    collection.document();

            customer.setCustomerId(
                    document.getId()
            );

        } else {

            document =
                    collection.document(
                            customer.getCustomerId()
                    );
        }

        // -------------------------------------------------
        // SAVE CUSTOMER PROFILE
        // -------------------------------------------------

        document
                .set(
                        customer
                )
                .get();

        // -------------------------------------------------
        // REGISTERED USER EXISTS -> SYNC COMMON DATA
        // -------------------------------------------------

        syncRegisteredUser(
                customer
        );
    }

    // =====================================================
    // GET CUSTOMER BY ID
    // =====================================================

    public Customer getCustomerById(
            String customerId
    ) throws ExecutionException,
            InterruptedException {

        customerId =
                clean(customerId);

        if (customerId == null) {

            return null;
        }

        // -------------------------------------------------
        // FIRST CHECK customers/{id}
        // -------------------------------------------------

        DocumentSnapshot customerDocument =
                firestore
                        .collection(
                                CUSTOMER_COLLECTION
                        )
                        .document(
                                customerId
                        )
                        .get()
                        .get();

        if (customerDocument.exists()) {

            return convertCustomerDocument(
                    customerDocument
            );
        }

        // -------------------------------------------------
        // THEN CHECK users/{id}
        // Registered Customer
        // -------------------------------------------------

        DocumentSnapshot userDocument =
                firestore
                        .collection(
                                USER_COLLECTION
                        )
                        .document(
                                customerId
                        )
                        .get()
                        .get();

        if (userDocument.exists()
                &&
                isCustomerRole(
                        userDocument.getString(
                                "role"
                        )
                )) {

            return convertUserDocumentToCustomer(
                    userDocument
            );
        }

        return null;
    }

    // =====================================================
    // GET ALL CUSTOMERS
    // =====================================================

    public List<Customer> getAllCustomers()
            throws ExecutionException,
            InterruptedException {

        /*
         * KEY:
         *
         * email असेल तर email वापरतो.
         * नाहीतर customerId.
         *
         * त्यामुळे users आणि customers मध्ये same
         * customer असेल तर Admin page वर duplicate दिसणार नाही.
         */

        Map<String, Customer> mergedCustomers =
                new LinkedHashMap<>();

        // =================================================
        // 1. CUSTOMERS COLLECTION
        // =================================================

        ApiFuture<QuerySnapshot> customerFuture =
                firestore
                        .collection(
                                CUSTOMER_COLLECTION
                        )
                        .get();

        List<QueryDocumentSnapshot> customerDocuments =
                customerFuture
                        .get()
                        .getDocuments();

        for (QueryDocumentSnapshot document :
                customerDocuments) {

            Customer customer =
                    convertCustomerDocument(
                            document
                    );

            if (customer == null) {

                continue;
            }

            String key =
                    getCustomerKey(
                            customer
                    );

            mergedCustomers.put(
                    key,
                    customer
            );
        }

        // =================================================
        // 2. USERS COLLECTION
        // Registered Users
        // =================================================

        ApiFuture<QuerySnapshot> userFuture =
                firestore
                        .collection(
                                USER_COLLECTION
                        )
                        .get();

        List<QueryDocumentSnapshot> userDocuments =
                userFuture
                        .get()
                        .getDocuments();

        for (QueryDocumentSnapshot document :
                userDocuments) {

            String role =
                    stringValue(
                            document.get(
                                    "role"
                            )
                    );

            /*
             * Admin / other roles Customers मध्ये
             * दाखवायचे नाहीत.
             */
            if (!isCustomerRole(role)) {

                continue;
            }

            Customer registeredCustomer =
                    convertUserDocumentToCustomer(
                            document
                    );

            if (registeredCustomer == null) {

                continue;
            }

            String key =
                    getCustomerKey(
                            registeredCustomer
                    );

            /*
             * customers collection मधला detailed profile
             * आधी असेल तर तो ठेवतो.
             *
             * Missing fields users मधून भरतो.
             */
            if (mergedCustomers.containsKey(key)) {

                Customer existingCustomer =
                        mergedCustomers.get(
                                key
                        );

                mergeMissingData(
                        existingCustomer,
                        registeredCustomer
                );

            } else {

                mergedCustomers.put(
                        key,
                        registeredCustomer
                );
            }
        }

        return new ArrayList<>(
                mergedCustomers.values()
        );
    }

    // =====================================================
    // UPDATE CUSTOMER
    // =====================================================

    public void updateCustomer(
            Customer customer
    ) throws ExecutionException,
            InterruptedException {

        if (customer == null) {

            throw new IllegalArgumentException(
                    "Customer cannot be null."
            );
        }

        String customerId =
                clean(
                        customer.getCustomerId()
                );

        if (customerId == null) {

            throw new IllegalArgumentException(
                    "Customer ID is required for update."
            );
        }

        // -------------------------------------------------
        // CLEAN VALUES
        // -------------------------------------------------

        customer.setName(
                cleanOrEmpty(
                        customer.getName()
                )
        );

        customer.setEmail(
                normalizeEmail(
                        customer.getEmail()
                )
        );

        customer.setPhone(
                cleanOrEmpty(
                        customer.getPhone()
                )
        );

        customer.setAddress(
                cleanOrEmpty(
                        customer.getAddress()
                )
        );

        customer.setCity(
                cleanOrEmpty(
                        customer.getCity()
                )
        );

        customer.setProfileImageUrl(
                cleanOrEmpty(
                        customer.getProfileImageUrl()
                )
        );

        if (clean(
                customer.getStatus()
        ) == null) {

            customer.setStatus(
                    "Active"
            );
        }

        // =================================================
        // CHECK customers/{customerId}
        // =================================================

        DocumentReference customerReference =
                firestore
                        .collection(
                                CUSTOMER_COLLECTION
                        )
                        .document(
                                customerId
                        );

        DocumentSnapshot customerDocument =
                customerReference
                        .get()
                        .get();

        // =================================================
        // FIND REGISTERED USER
        // =================================================

        DocumentSnapshot userDocument =
                findRegisteredUser(
                        customer
                );

        /*
         * जर ना customers document सापडला
         * ना registered user, तर invalid ID.
         */
        if (!customerDocument.exists()
                &&
                userDocument == null) {

            throw new IllegalArgumentException(
                    "Customer not found."
            );
        }

        // =================================================
        // UPDATE customers COLLECTION IF EXISTS
        // =================================================

        if (customerDocument.exists()) {

            /*
             * createdAt edit page कडून null आला
             * तर जुना preserve.
             */
            if (clean(
                    customer.getCreatedAt()
            ) == null) {

                customer.setCreatedAt(
                        customerDocument.getString(
                                "createdAt"
                        )
                );
            }

            customerReference
                    .set(
                            customer,
                            SetOptions.merge()
                    )
                    .get();
        }

        // =================================================
        // UPDATE REGISTERED users/{email}
        // =================================================

        if (userDocument != null) {

            updateUserCommonFields(
                    userDocument.getReference(),
                    customer
            );
        }
    }

    // =====================================================
    // DELETE CUSTOMER
    // =====================================================

    public void deleteCustomer(
            String customerId
    ) throws ExecutionException,
            InterruptedException {

        customerId =
                clean(customerId);

        if (customerId == null) {

            throw new IllegalArgumentException(
                    "Customer ID is required for delete."
            );
        }

        // =================================================
        // CUSTOMER DOCUMENT
        // =================================================

        DocumentReference customerReference =
                firestore
                        .collection(
                                CUSTOMER_COLLECTION
                        )
                        .document(
                                customerId
                        );

        DocumentSnapshot customerDocument =
                customerReference
                        .get()
                        .get();

        // =================================================
        // USER DOCUMENT BY ID
        // =================================================

        DocumentReference userReference =
                firestore
                        .collection(
                                USER_COLLECTION
                        )
                        .document(
                                customerId
                        );

        DocumentSnapshot userDocument =
                userReference
                        .get()
                        .get();

        // =================================================
        // IF USER NOT FOUND BY ID,
        // TRY CUSTOMER EMAIL
        // =================================================

        if ((!userDocument.exists()
                ||
                !isCustomerRole(
                        userDocument.getString(
                                "role"
                        )
                ))
                &&
                customerDocument.exists()) {

            String email =
                    customerDocument.getString(
                            "email"
                    );

            DocumentSnapshot byEmail =
                    findRegisteredUserByEmail(
                            email
                    );

            if (byEmail != null) {

                userReference =
                        byEmail.getReference();

                userDocument =
                        byEmail;
            }
        }

        // =================================================
        // BATCH DELETE
        // =================================================

        WriteBatch batch =
                firestore.batch();

        boolean hasOperation =
                false;

        if (customerDocument.exists()) {

            batch.delete(
                    customerReference
            );

            hasOperation =
                    true;
        }

        /*
         * Registered user/customer असेल तर users
         * document सुद्धा delete.
         *
         * Current project मध्ये login users collection
         * वरून होत असल्यामुळे यानंतर account login होणार नाही.
         */
        if (userDocument.exists()
                &&
                isCustomerRole(
                        userDocument.getString(
                                "role"
                        )
                )) {

            batch.delete(
                    userReference
            );

            hasOperation =
                    true;
        }

        if (hasOperation) {

            batch.commit()
                    .get();
        }
    }

    // =====================================================
    // SEARCH CUSTOMERS
    // =====================================================

    public List<Customer> searchCustomers(
            String searchText
    ) throws ExecutionException,
            InterruptedException {

        List<Customer> allCustomers =
                getAllCustomers();

        if (searchText == null
                ||
                searchText.isBlank()) {

            return allCustomers;
        }

        String search =
                searchText
                        .trim()
                        .toLowerCase();

        List<Customer> results =
                new ArrayList<>();

        for (Customer customer :
                allCustomers) {

            if (
                    contains(
                            customer.getCustomerId(),
                            search
                    )
                            ||
                            contains(
                                    customer.getName(),
                                    search
                            )
                            ||
                            contains(
                                    customer.getEmail(),
                                    search
                            )
                            ||
                            contains(
                                    customer.getPhone(),
                                    search
                            )
                            ||
                            contains(
                                    customer.getAddress(),
                                    search
                            )
                            ||
                            contains(
                                    customer.getCity(),
                                    search
                            )
                            ||
                            contains(
                                    customer.getStatus(),
                                    search
                            )
            ) {

                results.add(
                        customer
                );
            }
        }

        return results;
    }

    // =====================================================
    // FILTER CUSTOMERS BY STATUS
    // =====================================================

    public List<Customer> getCustomersByStatus(
            String status
    ) throws ExecutionException,
            InterruptedException {

        List<Customer> allCustomers =
                getAllCustomers();

        if (status == null
                ||
                status.isBlank()
                ||
                status.equalsIgnoreCase(
                        "All"
                )) {

            return allCustomers;
        }

        List<Customer> customers =
                new ArrayList<>();

        for (Customer customer :
                allCustomers) {

            String customerStatus =
                    clean(
                            customer.getStatus()
                    );

            /*
             * Registered accounts created by current
             * RegisterDAO do not contain status.
             *
             * Such accounts are treated as Active.
             */
            if (customerStatus == null) {

                customerStatus =
                        "Active";
            }

            if (customerStatus.equalsIgnoreCase(
                    status
            )) {

                customers.add(
                        customer
                );
            }
        }

        return customers;
    }

    // =====================================================
    // FIND REGISTERED USER FOR CUSTOMER
    // =====================================================

    private DocumentSnapshot findRegisteredUser(
            Customer customer
    ) throws ExecutionException,
            InterruptedException {

        if (customer == null) {

            return null;
        }

        // -------------------------------------------------
        // TRY customerId FIRST
        // -------------------------------------------------

        String customerId =
                clean(
                        customer.getCustomerId()
                );

        if (customerId != null) {

            DocumentSnapshot document =
                    firestore
                            .collection(
                                    USER_COLLECTION
                            )
                            .document(
                                    customerId
                            )
                            .get()
                            .get();

            if (document.exists()
                    &&
                    isCustomerRole(
                            document.getString(
                                    "role"
                            )
                    )) {

                return document;
            }
        }

        // -------------------------------------------------
        // TRY EMAIL
        // -------------------------------------------------

        return findRegisteredUserByEmail(
                customer.getEmail()
        );
    }

    // =====================================================
    // FIND USER BY EMAIL
    // =====================================================

    private DocumentSnapshot findRegisteredUserByEmail(
            String email
    ) throws ExecutionException,
            InterruptedException {

        String normalizedEmail =
                normalizeEmail(
                        email
                );

        if (normalizedEmail.isBlank()) {

            return null;
        }

        /*
         * Current RegisterDAO document ID itself is
         * normalized email, त्यामुळे direct lookup fast आहे.
         */
        DocumentSnapshot directDocument =
                firestore
                        .collection(
                                USER_COLLECTION
                        )
                        .document(
                                normalizedEmail
                        )
                        .get()
                        .get();

        if (directDocument.exists()
                &&
                isCustomerRole(
                        directDocument.getString(
                                "role"
                        )
                )) {

            return directDocument;
        }

        /*
         * Fallback query.
         * Future मध्ये document ID बदलला तरी काम करेल.
         */
        QuerySnapshot snapshot =
                firestore
                        .collection(
                                USER_COLLECTION
                        )
                        .whereEqualTo(
                                "email",
                                normalizedEmail
                        )
                        .limit(1)
                        .get()
                        .get();

        if (snapshot.isEmpty()) {

            return null;
        }

        DocumentSnapshot document =
                snapshot
                        .getDocuments()
                        .get(0);

        if (!isCustomerRole(
                stringValue(
                        document.get(
                                "role"
                        )
                )
        )) {

            return null;
        }

        return document;
    }

    // =====================================================
    // SYNC REGISTERED USER
    // =====================================================

    private void syncRegisteredUser(
            Customer customer
    ) throws ExecutionException,
            InterruptedException {

        DocumentSnapshot userDocument =
                findRegisteredUser(
                        customer
                );

        if (userDocument == null) {

            return;
        }

        updateUserCommonFields(
                userDocument.getReference(),
                customer
        );
    }

    // =====================================================
    // UPDATE COMMON USER FIELDS
    // =====================================================

    private void updateUserCommonFields(
            DocumentReference userReference,
            Customer customer
    ) throws ExecutionException,
            InterruptedException {

        if (userReference == null
                ||
                customer == null) {

            return;
        }

        Map<String, Object> updates =
                new LinkedHashMap<>();

        /*
         * Password field ला touch करत नाही.
         * Role field ला touch करत नाही.
         */

        updates.put(
                "name",
                cleanOrEmpty(
                        customer.getName()
                )
        );

        updates.put(
                "email",
                normalizeEmail(
                        customer.getEmail()
                )
        );

        updates.put(
                "phone",
                cleanOrEmpty(
                        customer.getPhone()
                )
        );

        updates.put(
                "address",
                cleanOrEmpty(
                        customer.getAddress()
                )
        );

        updates.put(
                "city",
                cleanOrEmpty(
                        customer.getCity()
                )
        );

        updates.put(
                "profileImageUrl",
                cleanOrEmpty(
                        customer.getProfileImageUrl()
                )
        );

        updates.put(
                "status",
                clean(
                        customer.getStatus()
                ) == null
                        ? "Active"
                        : customer.getStatus()
        );

        // -------------------------------------------------
        // LOCATION
        // -------------------------------------------------
        if (customer.getLatitude() != null
                && customer.getLongitude() != null
                && Double.isFinite(customer.getLatitude())
                && Double.isFinite(customer.getLongitude())
                && customer.getLatitude() >= -90.0
                && customer.getLatitude() <= 90.0
                && customer.getLongitude() >= -180.0
                && customer.getLongitude() <= 180.0) {

            updates.put(
                    "latitude",
                    customer.getLatitude()
            );

            updates.put(
                    "longitude",
                    customer.getLongitude()
            );

            if (clean(customer.getLocationUpdatedAt()) != null) {
                updates.put(
                        "locationUpdatedAt",
                        customer.getLocationUpdatedAt()
                );
            }
        }

        userReference
                .set(
                        updates,
                        SetOptions.merge()
                )
                .get();
    }

    // =====================================================
    // CUSTOMER DOCUMENT -> CUSTOMER MODEL
    // =====================================================

    private Customer convertCustomerDocument(
            DocumentSnapshot document
    ) {

        if (document == null
                ||
                !document.exists()) {

            return null;
        }

        /*
         * Do not use DocumentSnapshot.toObject(Customer.class) here.
         * Older Firestore records can contain numeric/Timestamp values
         * for fields that are Strings in the current model. One such
         * record used to abort the complete list and the controller then
         * returned an empty result.
         */
        String customerId =
                clean(
                        stringValue(
                                document.get(
                                        "customerId"
                                )
                        )
                );

        if (customerId == null) {

            customerId =
                    document.getId();
        }

        String email =
                normalizeEmail(
                        stringValue(
                                document.get(
                                        "email"
                                )
                        )
                );

        if (email.isBlank()
                && document.getId().contains("@")) {

            email =
                    normalizeEmail(
                            document.getId()
                    );
        }

        String status =
                clean(
                        stringValue(
                                document.get(
                                        "status"
                                )
                        )
                );

        if (status == null) {

            status =
                    "Active";
        }

        Customer customer = new Customer(
                customerId,
                cleanOrEmpty(stringValue(document.get("name"))),
                email,
                cleanOrEmpty(stringValue(document.get("phone"))),
                cleanOrEmpty(stringValue(document.get("address"))),
                cleanOrEmpty(stringValue(document.get("city"))),
                cleanOrEmpty(stringValue(document.get("profileImageUrl"))),
                status,
                cleanOrEmpty(stringValue(document.get("createdAt")))
        );

        customer.setLatitude(doubleValue(document.get("latitude")));
        customer.setLongitude(doubleValue(document.get("longitude")));
        customer.setLocationUpdatedAt(
                cleanOrEmpty(stringValue(document.get("locationUpdatedAt")))
        );

        return customer;
    }

    // =====================================================
    // users DOCUMENT -> CUSTOMER MODEL
    // =====================================================

    private Customer convertUserDocumentToCustomer(
            DocumentSnapshot document
    ) {

        if (document == null
                ||
                !document.exists()) {

            return null;
        }

        if (!isCustomerRole(
                stringValue(
                        document.get(
                                "role"
                        )
                )
        )) {

            return null;
        }

        String customerId =
                clean(
                        stringValue(
                                document.get(
                                        "customerId"
                                )
                        )
                );

        if (customerId == null) {

            customerId =
                    document.getId();
        }

        String name =
                cleanOrEmpty(
                        stringValue(document.get(
                                "name"
                        ))
                );

        String email =
                normalizeEmail(
                        stringValue(document.get(
                                "email"
                        ))
                );

        if (email.isBlank()
                && document.getId().contains("@")) {

            email =
                    normalizeEmail(
                            document.getId()
                    );
        }

        String phone =
                cleanOrEmpty(
                        stringValue(document.get(
                                "phone"
                        ))
                );

        String address =
                cleanOrEmpty(
                        stringValue(document.get(
                                "address"
                        ))
                );

        String city =
                cleanOrEmpty(
                        stringValue(document.get(
                                "city"
                        ))
                );

        String profileImageUrl =
                cleanOrEmpty(
                        stringValue(document.get(
                                "profileImageUrl"
                        ))
                );

        String status =
                clean(
                        stringValue(document.get(
                                "status"
                        ))
                );

        if (status == null) {

            status =
                    "Active";
        }

        String createdAt =
                cleanOrEmpty(
                        stringValue(document.get(
                                "createdAt"
                        ))
                );

        Customer customer = new Customer(
                customerId,
                name,
                email,
                phone,
                address,
                city,
                profileImageUrl,
                status,
                createdAt
        );

        customer.setLatitude(doubleValue(document.get("latitude")));
        customer.setLongitude(doubleValue(document.get("longitude")));
        customer.setLocationUpdatedAt(
                cleanOrEmpty(stringValue(document.get("locationUpdatedAt")))
        );

        return customer;
    }

    // =====================================================
    // MERGE DUPLICATE CUSTOMER DATA
    // =====================================================

    private void mergeMissingData(
            Customer target,
            Customer source
    ) {

        if (target == null
                ||
                source == null) {

            return;
        }

        if (clean(
                target.getName()
        ) == null) {

            target.setName(
                    source.getName()
            );
        }

        if (clean(
                target.getEmail()
        ) == null) {

            target.setEmail(
                    source.getEmail()
            );
        }

        if (clean(
                target.getPhone()
        ) == null) {

            target.setPhone(
                    source.getPhone()
            );
        }

        if (clean(
                target.getAddress()
        ) == null) {

            target.setAddress(
                    source.getAddress()
            );
        }

        if (clean(
                target.getCity()
        ) == null) {

            target.setCity(
                    source.getCity()
            );
        }

        if (clean(
                target.getProfileImageUrl()
        ) == null) {

            target.setProfileImageUrl(
                    source.getProfileImageUrl()
            );
        }

        if (clean(
                target.getStatus()
        ) == null) {

            target.setStatus(
                    source.getStatus()
            );
        }

        if (clean(
                target.getCreatedAt()
        ) == null) {

            target.setCreatedAt(
                    source.getCreatedAt()
            );
        }

        if (target.getLatitude() == null
                && source.getLatitude() != null) {

            target.setLatitude(
                    source.getLatitude()
            );
        }

        if (target.getLongitude() == null
                && source.getLongitude() != null) {

            target.setLongitude(
                    source.getLongitude()
            );
        }

        if (clean(target.getLocationUpdatedAt()) == null
                && clean(source.getLocationUpdatedAt()) != null) {

            target.setLocationUpdatedAt(
                    source.getLocationUpdatedAt()
            );
        }
    }

    // =====================================================
    // CUSTOMER KEY
    // =====================================================

    private String getCustomerKey(
            Customer customer
    ) {

        if (customer == null) {

            return "";
        }

        String email =
                normalizeEmail(
                        customer.getEmail()
                );

        if (!email.isBlank()) {

            return "EMAIL:"
                    + email;
        }

        String customerId =
                clean(
                        customer.getCustomerId()
                );

        if (customerId != null) {

            return "ID:"
                    + customerId;
        }

        return "UNKNOWN:"
                + System.identityHashCode(
                        customer
                );
    }

    // =====================================================
    // CUSTOMER ROLE CHECK
    // =====================================================

    private boolean isCustomerRole(
            String role
    ) {

        /*
         * Legacy accounts inside the users collection may not
         * contain a role field. AuthDAO already treats such a
         * document as a User based on its parent collection, so
         * customer management must use the same convention.
         */
        String normalizedRole =
                clean(role);

        if (normalizedRole == null) {

            return true;
        }

        return normalizedRole.equalsIgnoreCase(
                "User"
        )
                ||
                normalizedRole.equalsIgnoreCase(
                        "Customer"
                );
    }

    // =====================================================
    // HELPERS
    // =====================================================

    private String clean(
            String value
    ) {

        if (value == null) {

            return null;
        }

        String cleaned =
                value.trim();

        return cleaned.isEmpty()
                ? null
                : cleaned;
    }

    private String cleanOrEmpty(
            String value
    ) {

        String cleaned =
                clean(value);

        return cleaned == null
                ? ""
                : cleaned;
    }

    private String stringValue(
            Object value
    ) {

        if (value == null) {

            return null;
        }

        String text =
                String.valueOf(value).trim();

        return text.isEmpty()
                ? null
                : text;
    }

    private Double doubleValue(
            Object value
    ) {

        if (value == null) {
            return null;
        }

        if (value instanceof Number) {
            double number = ((Number) value).doubleValue();

            return Double.isFinite(number)
                    ? number
                    : null;
        }

        try {
            double number = Double.parseDouble(
                    String.valueOf(value).trim()
            );

            return Double.isFinite(number)
                    ? number
                    : null;

        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String normalizeEmail(
            String email
    ) {

        String cleaned =
                clean(email);

        if (cleaned == null) {

            return "";
        }

        return cleaned.toLowerCase();
    }

    private boolean contains(
            String value,
            String search
    ) {

        return value != null
                &&
                value
                        .toLowerCase()
                        .contains(
                                search
                        );
    }

    private String currentTime() {

        return String.valueOf(
                System.currentTimeMillis()
        );
    }
}
