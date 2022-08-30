package com.logreposit.logrepositapi.rest.dtos.request.ingress;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.logreposit.logrepositapi.persistence.documents.definition.DataType;
import com.logreposit.logrepositapi.rest.dtos.validation.ValidKeyName;
import javax.validation.constraints.NotNull;
import lombok.Data;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "datatype")
@JsonSubTypes({
  @JsonSubTypes.Type(value = StringFieldDto.class, name = "STRING"),
  @JsonSubTypes.Type(value = IntegerFieldDto.class, name = "INTEGER"),
  @JsonSubTypes.Type(value = FloatFieldDto.class, name = "FLOAT")
})
@Data
public class FieldDto {
  @ValidKeyName private String name;

  @NotNull private DataType datatype;
}
