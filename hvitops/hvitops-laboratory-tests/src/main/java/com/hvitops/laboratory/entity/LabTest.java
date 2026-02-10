package com.hvitops.laboratory.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.quarkus.mongodb.panache.PanacheMongoEntity;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class LabTest extends PanacheMongoEntity {
  // public ObjectId id;
  private Long patientId;

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime date;

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime performedAt;

  private String status;
  private List<TestItem> items;

  @Data
  public static class TestItem {
    private String testType;
    private String result;
    private String unit;
    private String referenceRange;
  }
}
