package com.logreposit.logrepositapi.rest.dtos.request.ingress;

import com.logreposit.logrepositapi.persistence.documents.definition.DataType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class IntegerFieldDto extends FieldDto {
  @NotNull private Long value;

  public IntegerFieldDto() {
    this.setDatatype(DataType.INTEGER);
  }
}
