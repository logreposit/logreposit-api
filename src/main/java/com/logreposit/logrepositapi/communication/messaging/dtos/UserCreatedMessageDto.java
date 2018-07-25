package com.logreposit.logrepositapi.communication.messaging.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class UserCreatedMessageDto
{
    private String       id;
    private String       email;
    private String       password;
    private List<String> roles;

    public String getId()
    {
        return this.id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getEmail()
    {
        return this.email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getPassword()
    {
        return this.password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public List<String> getRoles()
    {
        return this.roles;
    }

    public void setRoles(List<String> roles)
    {
        this.roles = roles;
    }
}
