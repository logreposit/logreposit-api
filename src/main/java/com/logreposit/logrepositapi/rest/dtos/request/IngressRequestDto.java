package com.logreposit.logrepositapi.rest.dtos.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.logreposit.logrepositapi.rest.dtos.DeviceType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class IngressRequestDto {
  @NotNull private DeviceType deviceType;

  @NotNull private Object data;
}
