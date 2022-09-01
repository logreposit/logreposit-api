package com.logreposit.logrepositapi.communication.messaging.mqtt.control;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateRoleCommand extends MosquittoControlApiCommand {
  public CreateRoleCommand(String roleName, String textName, String textDescription) {
    super("createRole");

    this.roleName = roleName;
    this.textName = textName;
    this.textDescription = textDescription;
  }

  @JsonProperty("rolename")
  private String roleName;

  @JsonProperty("textname")
  private String textName;

  @JsonProperty("textdescription")
  private String textDescription;
}
