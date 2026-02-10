package com.hvitops.appointments.dto;

import com.hvitops.appointments.entity.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentDTO {
    private Long id;
    private Long patientId;
    private Long doctorId;
    private LocalDateTime scheduledAt;
    private AppointmentStatus status;
    private String notes;
    private String medicalRecordId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
