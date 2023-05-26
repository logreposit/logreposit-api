package com.logreposit.logrepositapi.rest.dtos.shared.definition;

import com.logreposit.logrepositapi.rest.dtos.ResponseDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class DeviceDefinitionDto implements ResponseDto {
  @NotEmpty private List<@Valid MeasurementDefinitionDto> measurements;

  public DeviceDefinitionDto() {
    this.measurements = new ArrayList<>();
  }
}
