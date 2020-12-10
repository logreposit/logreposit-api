package com.logreposit.logrepositapi.communication.messaging.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
public class MessageMetaData
{
    private String userId;
    private String userEmail;
    private String deviceId;
    private String correlationId;
}
