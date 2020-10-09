package com.logreposit.logrepositapi.rest.dtos.request.ingress;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
public class ReadingDto
{
    @NotNull
    private Instant date;

    @NotBlank
    private String measurement;

    private List<@Valid TagDto> tags;

    @NotEmpty
    private List<@Valid FieldDto> fields;

    public ReadingDto()
    {
        this.tags   = new ArrayList<>();
        this.fields = new ArrayList<>();
    }
}
