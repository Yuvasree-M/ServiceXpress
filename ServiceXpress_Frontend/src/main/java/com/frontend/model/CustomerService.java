package com.frontend.model;

import com.frontend.model.Customer;
import com.frontend.model.ServiceStatus;
import com.frontend.model.ServiceHistory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomerService {

    public Customer getLoggedInCustomer() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("John Doe");
//        customer.setInitials("JD");
        return customer;
    }

    public int getCartCount(Long customerId) {
        // Mocking 3 items in cart
        return 3;
    }

    public List<ServiceStatus> getOngoingServices(Long customerId) {
        List<ServiceStatus> list = new ArrayList<>();

        ServiceStatus service1 = new ServiceStatus();
        service1.setVehicleType("Car");
        service1.setModel("Honda City");
        service1.setRegistration("KA05AB1234");
        service1.setService("Engine Checkup");
        service1.setCost(1200.00);
        service1.setStatus("In Progress");

        ServiceStatus service2 = new ServiceStatus();
        service2.setVehicleType("Bike");
        service2.setModel("Yamaha FZ");
        service2.setRegistration("KA01XY5678");
        service2.setService("Brake Replacement");
        service2.setCost(750.00);
        service2.setStatus("Pending");

        list.add(service1);
        list.add(service2);

        return list;
    }

    public List<ServiceHistory> getServiceHistory(Long customerId) {
        List<ServiceHistory> list = new ArrayList<>();

        ServiceHistory history1 = new ServiceHistory();
        history1.setDate("2025-03-15");
        history1.setVehicle("Honda City");
        history1.setWorkDone("Oil Change, Filter Replacement");
        history1.setCost(950.00);
        history1.setStatus("Completed");
        history1.setTransactionId("TXN123456");

        ServiceHistory history2 = new ServiceHistory();
        history2.setDate("2025-02-20");
        history2.setVehicle("Yamaha FZ");
        history2.setWorkDone("Chain Tightening");
        history2.setCost(300.00);
        history2.setStatus("Completed");
        history2.setTransactionId("TXN654321");

        list.add(history1);
        list.add(history2);

        return list;
    }
}
