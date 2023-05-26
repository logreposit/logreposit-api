package com.logreposit.logrepositapi.rest.dtos.request.ingress;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class IngressV2RequestDto {
  private List<@Valid ReadingDto> readings;

  public IngressV2RequestDto() {
    this.readings = new ArrayList<>();
  }
}
