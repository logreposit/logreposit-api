package com.logreposit.logrepositapi.rest.dtos.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.validation.constraints.NotBlank;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class DeviceCreationRequestDto {
  @NotBlank private String name;
}
