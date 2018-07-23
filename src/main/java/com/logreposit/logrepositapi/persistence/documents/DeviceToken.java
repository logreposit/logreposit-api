package com.logreposit.logrepositapi.persistence.documents;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Document
public class DeviceToken
{
    @Id
    private String id;

    @Indexed(unique = true)
    private String token;

    private String deviceId;
    private Date   createdAt;
}
