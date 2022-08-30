package com.logreposit.logrepositapi.rest.controllers;

import com.logreposit.logrepositapi.persistence.documents.Device;
import com.logreposit.logrepositapi.rest.dtos.ResponseDto;
import com.logreposit.logrepositapi.rest.dtos.common.SuccessResponse;
import com.logreposit.logrepositapi.rest.dtos.request.IngressRequestDto;
import com.logreposit.logrepositapi.rest.dtos.response.IngressResponseDto;
import com.logreposit.logrepositapi.services.ingress.IngressService;
import com.logreposit.logrepositapi.services.ingress.IngressServiceException;
import com.logreposit.logrepositapi.utils.duration.DurationCalculator;
import com.logreposit.logrepositapi.utils.duration.DurationCalculatorException;
import java.util.Date;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
public class IngressController {
  private final IngressService ingressService;
  private final DurationCalculator durationCalculator;

  public IngressController(IngressService ingressService, DurationCalculator durationCalculator) {
    this.ingressService = ingressService;
    this.durationCalculator = durationCalculator;
  }

  @PostMapping(path = "/v1/ingress")
  public ResponseEntity<SuccessResponse<ResponseDto>> ingress(
      Device device, @RequestBody @Valid IngressRequestDto ingressRequestDto)
      throws IngressServiceException, DurationCalculatorException {
    final var start = new Date();

    this.ingressService.processData(
        device, ingressRequestDto.getDeviceType(), ingressRequestDto.getData());

    long delta = this.durationCalculator.getDuration(start, new Date());

    return new ResponseEntity<>(buildResponse(delta), HttpStatus.ACCEPTED);
  }

  private static SuccessResponse<ResponseDto> buildResponse(long delta) {
    final var message =
        String.format("Data was accepted for processing in %d milliseconds.", delta);
    final var ingressResponseDto = new IngressResponseDto(message);

    return SuccessResponse.builder().data(ingressResponseDto).build();
  }
}
