package com.logreposit.logrepositapi.persistence.documents;

import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@NoArgsConstructor
@Data
@Document
public class DeviceToken {
  @Id private String id;

  @Indexed(unique = true)
  private String token;

  private String deviceId;
  private Date createdAt;
}
