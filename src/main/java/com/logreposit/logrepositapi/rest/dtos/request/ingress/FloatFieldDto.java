package com.logreposit.logrepositapi.rest.dtos.request.ingress;

import com.logreposit.logrepositapi.persistence.documents.definition.DataType;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class FloatFieldDto extends FieldDto {
  @NotNull private Double value;

  public FloatFieldDto() {
    this.setDatatype(DataType.FLOAT);
  }
}
