package com.backend.service;

import com.backend.model.ServiceCenter;
import com.backend.repository.ServiceCenterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

public interface ServiceCenterService {
    List<ServiceCenter> getAllCenters();
    ServiceCenter getCenterById(Long id); 
    ServiceCenter createCenter(ServiceCenter center);
    ServiceCenter updateCenter(Long id, ServiceCenter center);
    void deleteCenter(Long id);
}

@Service
class ServiceCenterServiceImpl implements ServiceCenterService {

    @Autowired
    private ServiceCenterRepository repository;

    @Override
    public List<ServiceCenter> getAllCenters() {
        return repository.findAll();
    }
    
    @Override
    public ServiceCenter getCenterById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public ServiceCenter createCenter(ServiceCenter center) {
        return repository.save(center);
    }

    @Override
    public ServiceCenter updateCenter(Long id, ServiceCenter center) {
        ServiceCenter existing = repository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("ServiceCenter not found with id: " + id));
        existing.setCityName(center.getCityName());
        existing.setCenterName(center.getCenterName());
        return repository.save(existing);
    }

    @Override
    public void deleteCenter(Long id) {
        if (!repository.existsById(id)) {
            throw new NoSuchElementException("ServiceCenter not found with id: " + id);
        }
        repository.deleteById(id);
    }
}
