package com.logreposit.logrepositapi.persistence.documents;

import java.util.Date;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@NoArgsConstructor
@Data
@Document
public class MqttCredential {
  @Id private String id;

  @Indexed(unique = true)
  private String username;

  private String password;
  private String description;

  private List<MqttRole> roles;

  private String userId;
  private Date createdAt;
}
