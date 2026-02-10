package com.hvitops.records.service;

import com.hvitops.records.entity.MedicalRecord;
import com.hvitops.records.repository.MedicalRecordRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;

@Singleton
public class MedicalRecordService {

  @Inject MedicalRecordRepository repository;

  public MedicalRecord createMedicalRecord(MedicalRecord record) {
    record.setCreatedAt(LocalDateTime.now());
    record.setUpdatedAt(LocalDateTime.now());
    repository.save(record);
    return record;
  }

  public Optional<MedicalRecord> getMedicalRecordById(String id) {
    return repository.findById(new ObjectId(id));
  }

  public List<MedicalRecord> getMedicalRecordsByPatientId(Long patientId) {
    return repository.findByPatientId(patientId);
  }

  public List<MedicalRecord> getMedicalRecordsByAppointmentId(Long appointmentId) {
    return repository.findByAppointmentId(appointmentId);
  }

  public MedicalRecord updateMedicalRecord(String id, MedicalRecord record) {
    Optional<MedicalRecord> existing = repository.findById(new ObjectId(id));
    if (existing.isPresent()) {
      MedicalRecord current = existing.get();
      current.setDiagnosis(record.getDiagnosis());
      current.setPrescriptions(record.getPrescriptions());
      current.setClinicalNotes(record.getClinicalNotes());
      current.setUpdatedAt(LocalDateTime.now());
      repository.update(current);
      return current;
    }
    return null;
  }

  public void deleteMedicalRecord(String id) {
    repository.deleteById(new ObjectId(id));
  }

  public List<MedicalRecord> getAllMedicalRecords() {
    return repository.findAll();
  }
}
