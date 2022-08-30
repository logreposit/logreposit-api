package com.logreposit.logrepositapi.persistence.documents;

import com.logreposit.logrepositapi.persistence.documents.definition.DeviceDefinition;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@NoArgsConstructor
@Data
@Document
public class Device {
  @Id private String id;

  private String userId;
  private String name;
  private DeviceDefinition definition;
}
