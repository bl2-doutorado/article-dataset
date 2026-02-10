package com.hvitops.appointments.service;

import com.hvitops.appointments.dto.AppointmentDTO;
import com.hvitops.appointments.dto.AvailableSlotDTO;
import com.hvitops.appointments.dto.DoctorDTO;
import com.hvitops.appointments.entity.Appointment;
import com.hvitops.appointments.entity.AppointmentStatus;
import com.hvitops.appointments.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentService {
    
    private final AppointmentRepository appointmentRepository;
    
    private static final Map<Long, DoctorDTO> DOCTORS = new HashMap<>();
    
    static {
        DOCTORS.put(1L, new DoctorDTO(1L, "Dr. João Silva", "Cardiologia"));
        DOCTORS.put(2L, new DoctorDTO(2L, "Dra. Maria Santos", "Pediatria"));
        DOCTORS.put(3L, new DoctorDTO(3L, "Dr. Pedro Oliveira", "Ortopedia"));
        DOCTORS.put(4L, new DoctorDTO(4L, "Dra. Ana Costa", "Dermatologia"));
    }
    
    @Transactional
    public AppointmentDTO createAppointment(AppointmentDTO dto) {
        if (dto.getScheduledAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Cannot schedule appointment in the past");
        }
        
        Appointment appointment = Appointment.builder()
                .patientId(dto.getPatientId())
                .doctorId(dto.getDoctorId())
                .scheduledAt(dto.getScheduledAt())
                .status(AppointmentStatus.SCHEDULED)
                .notes(dto.getNotes())
                .build();
        
        Appointment saved = appointmentRepository.save(appointment);
        return mapToDTO(saved);
    }
    
    @Transactional(readOnly = true)
    public AppointmentDTO getAppointmentById(Long id) {
        return appointmentRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
    }
    
    @Transactional(readOnly = true)
    public List<AppointmentDTO> getAppointmentsByPatientId(Long patientId) {
        return appointmentRepository.findByPatientId(patientId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<AppointmentDTO> getAppointmentsByDoctorId(Long doctorId) {
        return appointmentRepository.findByDoctorId(doctorId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public AppointmentDTO updateAppointment(Long id, AppointmentDTO dto) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        
        if (dto.getScheduledAt() != null && dto.getScheduledAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Cannot schedule appointment in the past");
        }
        
        if (dto.getScheduledAt() != null) {
            appointment.setScheduledAt(dto.getScheduledAt());
        }
        if (dto.getNotes() != null) {
            appointment.setNotes(dto.getNotes());
        }
        if (dto.getStatus() != null) {
            appointment.setStatus(dto.getStatus());
        }
        if (dto.getMedicalRecordId() != null) {
            appointment.setMedicalRecordId(dto.getMedicalRecordId());
        }
        
        Appointment updated = appointmentRepository.save(appointment);
        return mapToDTO(updated);
    }
    
    @Transactional
    public void cancelAppointment(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);
    }
    
    @Transactional(readOnly = true)
    public List<AppointmentDTO> getAllAppointments() {
        return appointmentRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<DoctorDTO> getDoctorsList() {
        return new ArrayList<>(DOCTORS.values());
    }
    
    @Transactional(readOnly = true)
    public List<DoctorDTO> getDoctorsBySpecialty(String specialty) {
        return DOCTORS.values().stream()
                .filter(d -> d.getSpecialty().equalsIgnoreCase(specialty))
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<String> getAllSpecialties() {
        return DOCTORS.values().stream()
                .map(DoctorDTO::getSpecialty)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<AvailableSlotDTO> getAvailableSlots(Long doctorId, LocalDate startDate, LocalDate endDate) {
        List<AvailableSlotDTO> slots = new ArrayList<>();
        
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            DayOfWeek dayOfWeek = current.getDayOfWeek();
            
            if (dayOfWeek.getValue() >= 1 && dayOfWeek.getValue() <= 5) {
                slots.addAll(generateDailySlots(doctorId, current));
            }
            
            current = current.plusDays(1);
        }
        
        return slots;
    }
    
    private List<AvailableSlotDTO> generateDailySlots(Long doctorId, LocalDate date) {
        List<AvailableSlotDTO> dailySlots = new ArrayList<>();
        
        int[] hours = {8, 9, 10, 11, 14, 15, 16, 17};
        
        for (int hour : hours) {
            LocalDateTime slotStart = LocalDateTime.of(date, LocalTime.of(hour, 0));
            LocalDateTime slotEnd = slotStart.plusHours(1);
            
            boolean isAvailable = isSlotAvailable(doctorId, slotStart, slotEnd);
            dailySlots.add(new AvailableSlotDTO(slotStart, slotEnd, isAvailable));
        }
        
        return dailySlots;
    }
    
    private boolean isSlotAvailable(Long doctorId, LocalDateTime slotStart, LocalDateTime slotEnd) {
        List<Appointment> appointments = appointmentRepository.findByDoctorId(doctorId);
        
        return appointments.stream()
                .filter(a -> a.getStatus() != AppointmentStatus.CANCELLED)
                .noneMatch(a -> {
                    LocalDateTime appointmentEnd = a.getScheduledAt().plusHours(1);
                    return !(slotEnd.isBefore(a.getScheduledAt()) || slotStart.isAfter(appointmentEnd));
                });
    }
    
    private AppointmentDTO mapToDTO(Appointment appointment) {
        return AppointmentDTO.builder()
                .id(appointment.getId())
                .patientId(appointment.getPatientId())
                .doctorId(appointment.getDoctorId())
                .scheduledAt(appointment.getScheduledAt())
                .status(appointment.getStatus())
                .notes(appointment.getNotes())
                .medicalRecordId(appointment.getMedicalRecordId())
                .createdAt(appointment.getCreatedAt())
                .updatedAt(appointment.getUpdatedAt())
                .build();
    }
}
