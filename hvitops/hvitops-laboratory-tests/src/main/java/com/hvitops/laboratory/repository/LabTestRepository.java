package com.hvitops.laboratory.repository;

import com.hvitops.laboratory.entity.LabTest;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class LabTestRepository implements PanacheMongoRepository<LabTest> {
    
    public List<LabTest> findByPatientId(Long patientId) {
        return list("patientId", patientId);
    }
    
    public LabTest findByIdAndPatientId(String id, Long patientId) {
        return find("_id = ?1 and patientId = ?2", id, patientId).firstResult();
    }
}
