package com.logreposit.logrepositapi.communication.messaging.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Getter
@Setter
public class UserCreatedMessageDto
{
    private String       id;
    private String       email;
    private String       password;
    private List<String> roles;
}
