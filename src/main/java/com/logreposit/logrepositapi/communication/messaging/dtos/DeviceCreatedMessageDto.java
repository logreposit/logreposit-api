package com.logreposit.logrepositapi.communication.messaging.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Getter
@Setter
public class DeviceCreatedMessageDto
{
    private String id;
    private String name;
}
