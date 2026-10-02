# Firebase Customer Visibility Fix

## What was fixed

- Admin Customer Management continues to merge records from both
  `customers` and `users`.
- Legacy documents in the `users` collection with a missing or blank
  `role` are now treated consistently with Login, which already identifies
  them as User accounts from their collection.
- Role values are trimmed before comparing `User` and `Customer`.
- New customer registrations now include `customerId`, `status`,
  `createdAt`, `address`, `city`, and `profileImageUrl` defaults in the
  existing `users/{normalized-email}` document.
- No duplicate `customers` document is created during registration.
  Customer Management's existing email-based merge remains the source of the
  combined Admin list.

## Files changed for this fix

- `src/main/java/project/dao/admin/CustomerDAO.java`
- `src/main/java/project/dao/auth/RegisterDAO.java`

## Windows verification

Run from the directory containing `pom.xml`:

```bat
mvn clean compile
mvn javafx:run
```

Then:

1. Log in as Admin and open Customers.
2. Click Refresh.
3. Confirm existing documents from `users` are visible.
4. Register a new User account.
5. Return to Admin Customers and click Refresh.
6. Confirm the new account appears only once.

Keep `guardian.json` private and never commit it to a public repository.
