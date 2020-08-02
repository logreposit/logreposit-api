package com.logreposit.logrepositapi.rest.dtos.request.ingress;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.logreposit.logrepositapi.persistence.documents.definition.DataType;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "datatype"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = StringFieldDto.class, name = "STRING"),
        @JsonSubTypes.Type(value = IntegerFieldDto.class, name = "INTEGER"),
        @JsonSubTypes.Type(value = FloatFieldDto.class, name = "FLOAT")
})
@Data
public class FieldDto
{
    @NotBlank
    private String name;

    @NotNull
    private DataType datatype;
}
