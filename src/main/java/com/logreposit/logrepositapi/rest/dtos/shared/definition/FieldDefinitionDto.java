package com.logreposit.logrepositapi.rest.dtos.shared.definition;

import com.logreposit.logrepositapi.persistence.documents.definition.DataType;
import com.logreposit.logrepositapi.rest.dtos.validation.ValidKeyName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FieldDefinitionDto {
  @NotBlank @ValidKeyName private String name;

  private String description;

  @NotNull private DataType datatype;
}
