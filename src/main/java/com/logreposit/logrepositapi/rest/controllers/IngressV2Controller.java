package com.logreposit.logrepositapi.rest.controllers;

import com.logreposit.logrepositapi.persistence.documents.Device;
import com.logreposit.logrepositapi.persistence.documents.definition.DeviceDefinition;
import com.logreposit.logrepositapi.rest.dtos.ResponseDto;
import com.logreposit.logrepositapi.rest.dtos.common.SuccessResponse;
import com.logreposit.logrepositapi.rest.dtos.request.ingress.IngressV2RequestDto;
import com.logreposit.logrepositapi.rest.dtos.response.IngressResponseDto;
import com.logreposit.logrepositapi.rest.dtos.shared.definition.DeviceDefinitionDto;
import com.logreposit.logrepositapi.rest.mappers.DeviceDefinitionMapper;
import com.logreposit.logrepositapi.services.device.DeviceService;
import com.logreposit.logrepositapi.services.device.DeviceServiceException;
import com.logreposit.logrepositapi.services.ingress.IngressService;
import com.logreposit.logrepositapi.services.ingress.IngressServiceException;
import com.logreposit.logrepositapi.utils.duration.DurationCalculator;
import com.logreposit.logrepositapi.utils.duration.DurationCalculatorException;
import jakarta.validation.Valid;
import java.util.Date;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
public class IngressV2Controller {
  private final DurationCalculator durationCalculator;
  private final DeviceService deviceService;
  private final IngressService ingressService;

  public IngressV2Controller(
      DurationCalculator durationCalculator,
      DeviceService deviceService,
      IngressService ingressService) {
    this.durationCalculator = durationCalculator;
    this.deviceService = deviceService;
    this.ingressService = ingressService;
  }

  @PutMapping(path = "/v2/ingress/definition")
  public ResponseEntity<SuccessResponse<ResponseDto>> ingressDefinition(
      Device device, @RequestBody @Valid DeviceDefinitionDto deviceDefinitionDto)
      throws DeviceServiceException {
    final var deviceDefinition = DeviceDefinitionMapper.toEntity(deviceDefinitionDto);
    final var updatedDefinition =
        this.deviceService.updateDefinition(device.getId(), deviceDefinition);

    return new ResponseEntity<>(buildDefinitionUpdatedDto(updatedDefinition), HttpStatus.OK);
  }

  @PostMapping(path = "/v2/ingress/data")
  public ResponseEntity<SuccessResponse<ResponseDto>> ingressData(
      Device device, @RequestBody @Valid IngressV2RequestDto ingressRequestDto)
      throws DurationCalculatorException, IngressServiceException {
    final var start = new Date();

    this.ingressService.processData(device, ingressRequestDto.getReadings());

    final var delta = this.durationCalculator.getDuration(start, new Date());

    return new ResponseEntity<>(buildIngressDataResponse(delta), HttpStatus.ACCEPTED);
  }

  private static SuccessResponse<ResponseDto> buildDefinitionUpdatedDto(
      DeviceDefinition deviceDefinition) {
    final var definition = DeviceDefinitionMapper.toDto(deviceDefinition);

    return SuccessResponse.builder().data(definition).build();
  }

  private static SuccessResponse<ResponseDto> buildIngressDataResponse(long delta) {
    final var message =
        String.format("Data was accepted for processing in %d milliseconds.", delta);
    final var ingressResponseDto = new IngressResponseDto(message);

    return SuccessResponse.builder().data(ingressResponseDto).build();
  }
}
