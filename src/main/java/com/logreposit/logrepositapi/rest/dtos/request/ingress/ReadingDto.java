package com.logreposit.logrepositapi.rest.dtos.request.ingress;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class ReadingDto
{
    @NotNull
    private Instant date;

    @NotBlank
    private String measurement;

    private Map<String, String> tags;

    @NotEmpty
    private List<FieldDto> fields;

    public ReadingDto() {
        this.tags = new HashMap<>();
        this.fields = new ArrayList<>();
    }
}
