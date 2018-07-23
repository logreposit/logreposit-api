package com.logreposit.logrepositapi.rest.controllers;

import com.logreposit.logrepositapi.persistence.documents.Device;
import com.logreposit.logrepositapi.rest.dtos.ResponseDto;
import com.logreposit.logrepositapi.rest.dtos.common.SuccessResponse;
import com.logreposit.logrepositapi.rest.dtos.request.LogIngressRequestDto;
import com.logreposit.logrepositapi.rest.dtos.response.IngressResponseDto;
import com.logreposit.logrepositapi.services.ingress.IngressService;
import com.logreposit.logrepositapi.services.ingress.IngressServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Date;

@RestController
@Validated
public class IngressController
{
    private final IngressService ingressService;

    public IngressController(IngressService ingressService)
    {
        this.ingressService = ingressService;
    }

    @RequestMapping(path = "/ingress", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SuccessResponse<ResponseDto>> ingress(Device device, @RequestBody @Valid LogIngressRequestDto logIngressRequestDto) throws IngressServiceException
    {
        Date start = new Date();

        this.ingressService.processData(device, logIngressRequestDto.getDeviceType(), logIngressRequestDto.getData());

        Date now   = new Date();
        long delta = now.getTime() - start.getTime();

        return new ResponseEntity<>(buildResponse(delta), HttpStatus.OK);
    }

    private static SuccessResponse<ResponseDto> buildResponse(long delta)
    {
        String message = String.format("Data was successfully processed in %d milliseconds.", delta);

        IngressResponseDto ingressResponseDto = new IngressResponseDto(message);

        SuccessResponse<ResponseDto> successResponse = SuccessResponse.builder().data(ingressResponseDto).build();

        return successResponse;
    }
}
