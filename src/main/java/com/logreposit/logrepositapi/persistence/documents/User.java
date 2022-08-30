package com.logreposit.logrepositapi.persistence.documents;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@Document
public class User {
  @Id private String id;

  @Indexed(unique = true)
  private String email;

  private String password;
  private List<String> roles;
}
