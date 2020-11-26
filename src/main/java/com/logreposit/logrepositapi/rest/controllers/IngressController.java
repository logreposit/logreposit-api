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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Date;

@RestController
@Validated
public class IngressController
{
    private final IngressService     ingressService;
    private final DurationCalculator durationCalculator;

    public IngressController(IngressService ingressService, DurationCalculator durationCalculator)
    {
        this.ingressService     = ingressService;
        this.durationCalculator = durationCalculator;
    }

    @PostMapping(path = {"/ingress", "/v1/ingress"})
    public ResponseEntity<SuccessResponse<ResponseDto>> ingress(Device device, @RequestBody @Valid IngressRequestDto ingressRequestDto)
            throws IngressServiceException, DurationCalculatorException
    {
        Date start = new Date();

        this.ingressService.processData(device, ingressRequestDto.getDeviceType(), ingressRequestDto.getData());

        Date now   = new Date();
        long delta = this.durationCalculator.getDuration(start, now);

        return new ResponseEntity<>(buildResponse(delta), HttpStatus.ACCEPTED);
    }

    private static SuccessResponse<ResponseDto> buildResponse(long delta)
    {
        String                       message            = String.format("Data was accepted for processing in %d milliseconds.", delta);
        IngressResponseDto           ingressResponseDto = new IngressResponseDto(message);
        SuccessResponse<ResponseDto> successResponse    = SuccessResponse.builder().data(ingressResponseDto).build();

        return successResponse;
    }
}
