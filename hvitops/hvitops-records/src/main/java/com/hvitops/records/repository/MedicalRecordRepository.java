package com.hvitops.records.repository;

import com.hvitops.records.entity.MedicalRecord;
import io.micronaut.data.mongodb.annotation.MongoRepository;
import io.micronaut.data.repository.CrudRepository;
import org.bson.types.ObjectId;

import java.util.List;

@MongoRepository
public interface MedicalRecordRepository extends CrudRepository<MedicalRecord, ObjectId> {
    List<MedicalRecord> findByPatientId(Long patientId);
    List<MedicalRecord> findByAppointmentId(Long appointmentId);
}
