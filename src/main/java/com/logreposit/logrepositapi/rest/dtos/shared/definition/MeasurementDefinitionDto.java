package com.logreposit.logrepositapi.rest.dtos.shared.definition;

import com.logreposit.logrepositapi.rest.dtos.validation.ValidKeyName;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class MeasurementDefinitionDto {
  @ValidKeyName private String name;

  private Set<@ValidKeyName String> tags;

  @NotEmpty private List<@Valid FieldDefinitionDto> fields;

  public MeasurementDefinitionDto() {
    this.tags = new HashSet<>();
    this.fields = new ArrayList<>();
  }
}
