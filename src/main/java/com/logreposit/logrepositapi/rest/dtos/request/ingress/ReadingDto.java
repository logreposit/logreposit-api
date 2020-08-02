package com.logreposit.logrepositapi.rest.dtos.request.ingress;

import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
public class ReadingDto
{
    private Instant date;
    private String measurement;
    private Map<String, String> tags;
    private List<FieldDto> fields;
}

