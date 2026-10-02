package project.controller.admin;

import com.google.cloud.firestore.Firestore;
import project.dao.admin.CustomerDAO;
import project.firebase.FirebaseConfig;
import project.model.Customer;

import java.util.List;

public class CustomerController {

    private final CustomerDAO customerDAO;

    public CustomerController() throws Exception {

        Firestore firestore =
                FirebaseConfig.getFirestore();

        customerDAO =
                new CustomerDAO(firestore);
    }

    public boolean addCustomer(
            Customer customer
    ) {

        try {

            customerDAO.addCustomer(
                    customer
            );

            return true;

        } catch (
                Exception e
        ) {

            e.printStackTrace();

            return false;
        }
    }

    public Customer getCustomer(
            String customerId
    ) {

        try {

            return customerDAO.getCustomerById(
                    customerId
            );

        } catch (
                Exception e
        ) {

            e.printStackTrace();

            return null;
        }
    }

    public List<Customer> getAllCustomers() {

        try {

            return customerDAO.getAllCustomers();

        } catch (
                Exception e
        ) {

            e.printStackTrace();

            return List.of();
        }
    }

    public boolean updateCustomer(
            Customer customer
    ) {

        try {

            customerDAO.updateCustomer(
                    customer
            );

            return true;

        } catch (
                Exception e
        ) {

            e.printStackTrace();

            return false;
        }
    }

    public boolean deleteCustomer(
            String customerId
    ) {

        try {

            customerDAO.deleteCustomer(
                    customerId
            );

            return true;

        } catch (
                Exception e
        ) {

            e.printStackTrace();

            return false;
        }
    }

    public List<Customer> searchCustomers(
            String searchText
    ) {

        try {

            return customerDAO.searchCustomers(
                    searchText
            );

        } catch (
                Exception e
        ) {

            e.printStackTrace();

            return List.of();
        }
    }

    public List<Customer> getCustomersByStatus(
            String status
    ) {

        try {

            return customerDAO.getCustomersByStatus(
                    status
            );

        } catch (
                Exception e
        ) {

            e.printStackTrace();

            return List.of();
        }
    }
}