package com.logreposit.logrepositapi.rest.controllers;

import com.logreposit.logrepositapi.persistence.documents.Device;
import com.logreposit.logrepositapi.rest.dtos.ResponseDto;
import com.logreposit.logrepositapi.rest.dtos.common.SuccessResponse;
import com.logreposit.logrepositapi.rest.dtos.request.LogIngressRequestDto;
import com.logreposit.logrepositapi.rest.dtos.response.DeviceResponseDto;
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
    public IngressController()
    {
    }

    @RequestMapping(path = "/ingress", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SuccessResponse<ResponseDto>> ingress(Device device, @RequestBody @Valid LogIngressRequestDto logIngressRequestDto)
    {
        return new ResponseEntity<>(buildResponse(device), HttpStatus.OK);
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
