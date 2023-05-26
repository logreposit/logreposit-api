package com.logreposit.logrepositapi.rest.dtos.request.ingress;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class ReadingDto {
  @NotNull private Instant date;

  @NotBlank private String measurement;

  private List<@Valid TagDto> tags;

  @NotEmpty private List<@Valid FieldDto> fields;

  public ReadingDto() {
    this.tags = new ArrayList<>();
    this.fields = new ArrayList<>();
  }
}
