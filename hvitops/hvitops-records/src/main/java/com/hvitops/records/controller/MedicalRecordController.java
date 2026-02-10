package com.hvitops.records.controller;

import com.hvitops.records.entity.MedicalRecord;
import com.hvitops.records.service.MedicalRecordService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import jakarta.inject.Inject;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Optional;

@Controller("/records")
// @CrossOrigin(value = "*", maxAge = 3600)
public class MedicalRecordController {
    
    @Inject
    MedicalRecordService service;
    
    @Post
    public HttpResponse<MedicalRecord> createMedicalRecord(@Body MedicalRecord record) {
        MedicalRecord created = service.createMedicalRecord(record);
        return HttpResponse.created(created);
    }
    
    @Get("/{id}")
    public HttpResponse<MedicalRecord> getMedicalRecord(@PathVariable String id) {
        Optional<MedicalRecord> record = service.getMedicalRecordById(id);
        return record.map(HttpResponse::ok).orElse(HttpResponse.notFound());
    }
    
    @Get
    public HttpResponse<List<MedicalRecord>> getAllMedicalRecords() {
        List<MedicalRecord> records = service.getAllMedicalRecords();
        return HttpResponse.ok(records);
    }
    
    @Get("/patient/{patientId}")
    public HttpResponse<List<MedicalRecord>> getMedicalRecordsByPatient(@PathVariable Long patientId) {
        List<MedicalRecord> records = service.getMedicalRecordsByPatientId(patientId);
        return HttpResponse.ok(records);
    }
    
    @Get("/appointment/{appointmentId}")
    public HttpResponse<List<MedicalRecord>> getMedicalRecordsByAppointment(@PathVariable Long appointmentId) {
        List<MedicalRecord> records = service.getMedicalRecordsByAppointmentId(appointmentId);
        return HttpResponse.ok(records);
    }
    
    @Put("/{id}")
    public HttpResponse<MedicalRecord> updateMedicalRecord(@PathVariable String id, @Body MedicalRecord record) {
        MedicalRecord updated = service.updateMedicalRecord(id, record);
        if (updated != null) {
            return HttpResponse.ok(updated);
        }
        return HttpResponse.notFound();
    }
    
    @Delete("/{id}")
    public HttpResponse<Void> deleteMedicalRecord(@PathVariable String id) {
        service.deleteMedicalRecord(id);
        return HttpResponse.noContent();
    }
}
