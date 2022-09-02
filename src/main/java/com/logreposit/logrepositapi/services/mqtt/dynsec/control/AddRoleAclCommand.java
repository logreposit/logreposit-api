package com.logreposit.logrepositapi.services.mqtt.dynsec.control;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AddRoleAclCommand extends MosquittoControlApiCommand {
  public AddRoleAclCommand(String roleName, String aclType, String topic, boolean allow) {
    super("addRoleACL");

    this.roleName = roleName;
    this.aclType = aclType;
    this.topic = topic;
    this.allow = allow;
  }

  @JsonProperty("rolename")
  private String roleName;

  @JsonProperty("acltype")
  private String aclType;

  private String topic;
  private boolean allow;
  private Integer priority;
}
