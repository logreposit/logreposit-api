package com.logreposit.logrepositapi.rest.dtos.request.ingress;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class IngressV2RequestDto
{
    private List<ReadingDto> readings;

    public IngressV2RequestDto() {
        this.readings = new ArrayList<>();
    }
}
