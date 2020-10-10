package com.logreposit.logrepositapi.rest.dtos.request.ingress;

import com.logreposit.logrepositapi.persistence.documents.definition.DataType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;

@Data
@EqualsAndHashCode(callSuper = true)
public class StringFieldDto extends FieldDto
{
    @NotBlank
    private String value;

    public StringFieldDto()
    {
        this.setDatatype(DataType.STRING);
    }
}
