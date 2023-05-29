package com.logreposit.logrepositapi.services.mqtt.mosquitto.dynsec.control.commands;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateRoleCommand extends MosquittoControlApiCommand {
  public CreateRoleCommand(String roleName) {
    super("createRole");

    this.roleName = roleName;
  }

  @JsonProperty("rolename")
  private String roleName;

  @JsonProperty("textname")
  private String textName;

  @JsonProperty("textdescription")
  private String textDescription;
}
