package com.hvitops.appointments.controller;

import com.hvitops.appointments.dto.AppointmentDTO;
import com.hvitops.appointments.dto.AvailableSlotDTO;
import com.hvitops.appointments.dto.DoctorDTO;
import com.hvitops.appointments.service.AppointmentService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
public class AppointmentController {

  private final AppointmentService appointmentService;

  @PostMapping
  public ResponseEntity<AppointmentDTO> createAppointment(@RequestBody AppointmentDTO dto) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(appointmentService.createAppointment(dto));
  }

  @GetMapping("/{id}")
  public ResponseEntity<AppointmentDTO> getAppointment(@PathVariable Long id) {
    return ResponseEntity.ok(appointmentService.getAppointmentById(id));
  }

  @GetMapping
  public ResponseEntity<List<AppointmentDTO>> getAllAppointments() {
    return ResponseEntity.ok(appointmentService.getAllAppointments());
  }

  @GetMapping("/patient/{patientId}")
  public ResponseEntity<List<AppointmentDTO>> getAppointmentsByPatient(
      @PathVariable Long patientId) {
    return ResponseEntity.ok(appointmentService.getAppointmentsByPatientId(patientId));
  }

  @GetMapping("/doctor/{doctorId}")
  public ResponseEntity<List<AppointmentDTO>> getAppointmentsByDoctor(@PathVariable Long doctorId) {
    return ResponseEntity.ok(appointmentService.getAppointmentsByDoctorId(doctorId));
  }

  @PutMapping("/{id}")
  public ResponseEntity<AppointmentDTO> updateAppointment(
      @PathVariable Long id, @RequestBody AppointmentDTO dto) {
    return ResponseEntity.ok(appointmentService.updateAppointment(id, dto));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> cancelAppointment(@PathVariable Long id) {
    appointmentService.cancelAppointment(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/doctors/list")
  public ResponseEntity<List<DoctorDTO>> getDoctorsList() {
    return ResponseEntity.ok(appointmentService.getDoctorsList());
  }

  @GetMapping("/specialties/list")
    public ResponseEntity<List<String>> getAllSpecialties() {
        return ResponseEntity.ok(appointmentService.getAllSpecialties());
    }
    
    @GetMapping("/doctors/specialty/{specialty}")
    public ResponseEntity<List<DoctorDTO>> getDoctorsBySpecialty(@PathVariable String specialty) {
        return ResponseEntity.ok(appointmentService.getDoctorsBySpecialty(specialty));
    }
    
    @GetMapping("/slots/{doctorId}")
    public ResponseEntity<List<AvailableSlotDTO>> getAvailableSlots(
            @PathVariable Long doctorId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        return ResponseEntity.ok(appointmentService.getAvailableSlots(doctorId, startDate, endDate));
    }
}
