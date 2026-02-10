package com.hvitops.laboratory.service;

import com.hvitops.laboratory.entity.LabTest;
import com.hvitops.laboratory.entity.LabTestType;
import com.hvitops.laboratory.repository.LabTestRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import org.bson.types.ObjectId;

@ApplicationScoped
public class LabTestService {

  @Inject LabTestRepository repository;

  public LabTest createLabTest(LabTest labTest) {
    labTest.persist();
    return labTest;
  }

  public LabTest getLabTestById(String id) {
    ObjectId objectId = new ObjectId(id);
    return repository.findById(objectId);
  }

  public List<LabTest> getLabTestsByPatientId(Long patientId) {
    return repository.findByPatientId(patientId);
  }

  public LabTest updateLabTest(String id, LabTest labTest) {
    ObjectId objectId = new ObjectId(id);
    LabTest existing = repository.findById(objectId);

    if (existing != null) {
      existing.setDate(labTest.getDate());
      existing.setStatus(labTest.getStatus());
      if (labTest.getItems() != null && !labTest.getItems().isEmpty()) {
        existing.setItems(labTest.getItems());
      }
      if (labTest.getPatientId() != null) {
        existing.setPatientId(labTest.getPatientId());
      }
      existing.update();
    }
    return existing;
  }

  public void deleteLabTest(String id) {
    ObjectId objectId = new ObjectId(id);
    repository.deleteById(objectId);
  }

  public List<LabTest> getAllLabTests() {
    return repository.listAll();
  }

  public List<LabTestType> getTestTypes() {
    List<LabTestType> types = new ArrayList<>();
    types.add(
        new LabTestType("blood_count", "Hemograma Completo", "Contagem de celulas sanguineas"));
    types.add(new LabTestType("glucose", "Glicose em Jejum", "Teste de glicose no sangue"));
    types.add(new LabTestType("cholesterol", "Perfil Lipidico", "Colesterol e triglicerideos"));
    types.add(new LabTestType("liver_function", "Funcao Hepatica", "Testes de funcao do figado"));
    types.add(new LabTestType("kidney_function", "Funcao Renal", "Testes de funcao dos rins"));
    types.add(new LabTestType("thyroid", "Funcao Tireoidiana", "TSH e hormonios da tireoide"));
    types.add(new LabTestType("urinalysis", "Urinalise", "Analise de urina"));
    types.add(new LabTestType("covid_test", "Teste COVID-19", "PCR ou Antigeno COVID-19"));
    return types;
  }
}
