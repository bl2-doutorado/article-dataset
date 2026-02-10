package com.hvitops.records.entity;

import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.serde.annotation.Serdeable;
import java.time.LocalDateTime;
import java.util.List;
import org.bson.types.ObjectId;

@Serdeable // Permite que o Micronaut transforme o objeto em JSON/BSON
@MappedEntity // Diz ao Micronaut Data que isso é uma coleção no MongoDB
public class MedicalRecord {
  @Id // io.micronaut.data.annotation.Id
  private ObjectId id; // Use private e adicione Getter/Setter para evitar problemas de introspecção

  // Adicione Getters e Setters para o campo 'id'
  public ObjectId getId() {
    return id;
  }

  public void setId(ObjectId id) {
    this.id = id;
  }

  private Long patientId;
  public Long getPatientId() {
    return patientId;
  }

  public void setPatientId(Long patientId) {
    this.patientId = patientId;
  }



  public LocalDateTime getDate() {
    return date;
  }

  public void setDate(LocalDateTime date) {
    this.date = date;
  }

  public String getDiagnosis() {
    return diagnosis;
  }

  public void setDiagnosis(String diagnosis) {
    this.diagnosis = diagnosis;
  }

  public List<String> getPrescriptions() {
    return prescriptions;
  }

  public void setPrescriptions(List<String> prescriptions) {
    this.prescriptions = prescriptions;
  }

  public String getClinicalNotes() {
    return clinicalNotes;
  }

  public void setClinicalNotes(String clinicalNotes) {
    this.clinicalNotes = clinicalNotes;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

public Long getAppointmentId() {
    return appointmentId;
  }

  public void setAppointmentId(Long appointmentId) {
  this.appointmentId = appointmentId;
}

  public void setappointmentId(Long appointmentId) {
    this.appointmentId = appointmentId;
  }

  private Long appointmentId;
  private LocalDateTime date;
  private String diagnosis;
  private List<String> prescriptions;
  private String clinicalNotes;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
