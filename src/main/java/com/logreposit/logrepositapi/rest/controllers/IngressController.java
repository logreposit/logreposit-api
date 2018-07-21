package com.logreposit.logrepositapi.rest.controllers;

import com.logreposit.logrepositapi.persistence.documents.Device;
import com.logreposit.logrepositapi.rest.dtos.ResponseDto;
import com.logreposit.logrepositapi.rest.dtos.common.SuccessResponse;
import com.logreposit.logrepositapi.rest.dtos.request.LogIngressRequestDto;
import com.logreposit.logrepositapi.rest.dtos.response.DeviceResponseDto;
import com.logreposit.logrepositapi.services.ingress.IngressService;
import com.logreposit.logrepositapi.services.ingress.IngressServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
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
        this.ingressService.processData(device, logIngressRequestDto.getDeviceType(), logIngressRequestDto.getData());

        return new ResponseEntity<>(buildResponse(device), HttpStatus.OK); // TODO: meaningful response
    }

    private static SuccessResponse<ResponseDto> buildResponse(Device device)
    {
        DeviceResponseDto deviceResponseDto = new DeviceResponseDto();

        deviceResponseDto.setId(device.getId());
        deviceResponseDto.setName(device.getName());

        SuccessResponse<ResponseDto> successResponse = SuccessResponse.builder().data(deviceResponseDto).build();

        return successResponse;
    }
}
