package com.logreposit.logrepositapi.persistence.documents;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Document
public class User
{
    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    private String       password;
    private List<String> roles;
}
