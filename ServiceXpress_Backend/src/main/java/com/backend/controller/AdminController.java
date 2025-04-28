//package com.backend.controller;
//
//import com.backend.model.*;
//import com.backend.service.AdminService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.access.annotation.Secured;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/admin")
//public class AdminController {
//
//    @Autowired
//    private AdminService adminService;
//
//    // Admin CRUD for service packages
//    @Secured("ROLE_ADMIN")
//    @PostMapping("/service-packages")
//    public ServicePackage createServicePackage(@RequestBody ServicePackage servicePackage) {
//        return adminService.saveServicePackage(servicePackage);
//    }
//
//    @Secured("ROLE_ADMIN")
//    @PutMapping("/service-packages/{id}")
//    public ServicePackage updateServicePackage(@PathVariable Long id, @RequestBody ServicePackage servicePackage) {
//        servicePackage.setId(id);
//        return adminService.saveServicePackage(servicePackage);
//    }
//
//    @Secured("ROLE_ADMIN")
//    @DeleteMapping("/service-packages/{id}")
//    public void deleteServicePackage(@PathVariable Long id) {
//        adminService.deleteServicePackage(id);
//    }
//
//    // Admin assigns service request to a service advisor
//    @Secured("ROLE_ADMIN")
//    @PostMapping("/service-requests/{requestId}/assign/{advisorId}")
//    public CustomerServiceRequests assignServiceRequest(@PathVariable Integer requestId, @PathVariable Long advisorId) {
//        return adminService.assignServiceRequestToAdvisor(requestId, advisorId);
//    }
//
//    // Admin marks service request as completed
//    @Secured("ROLE_ADMIN")
//    @PutMapping("/service-requests/{requestId}/complete")
//    public CustomerServiceRequests completeServiceRequest(@PathVariable Integer requestId) {
//        return adminService.completeServiceRequest(requestId);
//    }
//
//    // Admin sends bill of materials to Admin
//    @Secured("ROLE_ADMIN")
//    @PostMapping("/service-requests/{requestId}/send-bill")
//    public void sendBillToAdmin(@PathVariable Integer requestId) {
//        adminService.sendBillToAdminById(requestId);
//    }
//
//    // Admin sends payment link to customer
//    @Secured("ROLE_ADMIN")
//    @PostMapping("/service-requests/{requestId}/send-payment-url")
//    public void sendPaymentUrlToCustomer(@PathVariable Integer requestId) {
//        adminService.sendPaymentUrlToCustomerById(requestId);
//    }
//}
