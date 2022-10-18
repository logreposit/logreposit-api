package com.logreposit.logrepositapi.services.mqtt.dynsec.control.commands;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AddClientRoleCommand extends MosquittoControlApiCommand {
  public AddClientRoleCommand(String username, String roleName) {
    super("addClientRole");

    this.username = username;
    this.roleName = roleName;
  }

  private String username;

  @JsonProperty("rolename")
  private String roleName;
}
