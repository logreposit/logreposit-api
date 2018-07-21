package com.logreposit.logrepositapi.rest.controllers;

import com.logreposit.logrepositapi.persistence.documents.Device;
import com.logreposit.logrepositapi.persistence.documents.User;
import com.logreposit.logrepositapi.rest.dtos.ResponseDto;
import com.logreposit.logrepositapi.rest.dtos.common.SuccessResponse;
import com.logreposit.logrepositapi.rest.dtos.request.DeviceCreationRequestDto;
import com.logreposit.logrepositapi.rest.dtos.response.DeviceResponseDto;
import com.logreposit.logrepositapi.services.devices.DeviceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
public class DeviceController
{
    private static final Logger logger = LoggerFactory.getLogger(DeviceController.class);

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService)
    {
        this.deviceService = deviceService;
    }

    @RequestMapping(path = "/devices", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SuccessResponse<ResponseDto>> create(@Valid @RequestBody DeviceCreationRequestDto deviceCreationRequestDto,
                                                              User authenticatedUser)
    {
        Device                       device            = buildDevice(deviceCreationRequestDto, authenticatedUser.getId());
        Device                       createdDevice     = this.deviceService.create(device);
        DeviceResponseDto            deviceResponseDto = convertDevice(device);
        SuccessResponse<ResponseDto> successResponse   = SuccessResponse.builder().data(deviceResponseDto).build();

        logger.info("Successfully created device: {}", createdDevice);

        return new ResponseEntity<>(successResponse, HttpStatus.CREATED);
    }

    private static Device buildDevice(DeviceCreationRequestDto deviceCreationRequestDto, String userId)
    {
        Device device = new Device();

        device.setName(deviceCreationRequestDto.getName());
        device.setUserId(userId);

        return device;
    }

    private static DeviceResponseDto convertDevice(Device device)
    {
        DeviceResponseDto deviceResponseDto = new DeviceResponseDto();

        deviceResponseDto.setId(device.getId());
        deviceResponseDto.setName(device.getName());

        return deviceResponseDto;
    }
}
