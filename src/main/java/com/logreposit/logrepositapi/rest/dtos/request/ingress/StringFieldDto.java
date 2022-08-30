package com.logreposit.logrepositapi.rest.dtos.request.ingress;

import com.logreposit.logrepositapi.persistence.documents.definition.DataType;
import javax.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StringFieldDto extends FieldDto {
  @NotBlank private String value;

  public StringFieldDto() {
    this.setDatatype(DataType.STRING);
  }
}
