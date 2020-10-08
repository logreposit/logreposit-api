package com.logreposit.logrepositapi.rest.dtos.request.ingress;

import com.logreposit.logrepositapi.persistence.documents.definition.DataType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class IntegerFieldDto extends FieldDto
{
    @NotNull
    private Integer value;

    public IntegerFieldDto() {
        this.setDatatype(DataType.INTEGER);
    }
}
