package com.logreposit.logrepositapi.persistence.documents;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.logreposit.logrepositapi.rest.dtos.request.MqttCredentialRequestDto;
import com.logreposit.logrepositapi.rest.dtos.response.MqttCredentialResponseDto;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

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

  public MqttCredential() {
    this.roles = new ArrayList<>();
  }
}
