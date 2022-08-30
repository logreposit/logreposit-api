package com.logreposit.logrepositapi.rest.dtos.request.ingress;

import com.logreposit.logrepositapi.rest.dtos.validation.ValidKeyName;
import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TagDto {
  @ValidKeyName private String name;

  @NotBlank private String value;
}
