package com.logreposit.logrepositapi.persistence.documents.definition;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class DeviceDefinition {
  private List<MeasurementDefinition> measurements;

  public DeviceDefinition() {
    this.measurements = new ArrayList<>();
  }
}
