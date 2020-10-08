package com.logreposit.logrepositapi.rest.controllers;

import com.logreposit.logrepositapi.persistence.documents.DeviceToken;
import com.logreposit.logrepositapi.persistence.documents.User;
import com.logreposit.logrepositapi.rest.dtos.ResponseDto;
import com.logreposit.logrepositapi.rest.dtos.common.SuccessResponse;
import com.logreposit.logrepositapi.rest.dtos.response.DeviceTokenResponseDto;
import com.logreposit.logrepositapi.rest.dtos.response.PaginationResponseDto;
import com.logreposit.logrepositapi.services.common.DeviceTokenNotFoundException;
import com.logreposit.logrepositapi.services.device.DeviceNotFoundException;
import com.logreposit.logrepositapi.services.devicetoken.DeviceTokenService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Validated
public class DeviceTokenController
{
    private final DeviceTokenService deviceTokenService;

    public DeviceTokenController(DeviceTokenService deviceTokenService)
    {
        this.deviceTokenService = deviceTokenService;
    }

    @RequestMapping(path = {"/devices/{deviceId}/tokens", "/v1/devices/{deviceId}/tokens"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SuccessResponse<ResponseDto>> create(@PathVariable("deviceId") String deviceId,
                                                               User authenticatedUser) throws DeviceNotFoundException
    {
        DeviceToken                  deviceToken            = this.deviceTokenService.create(deviceId, authenticatedUser.getId());
        DeviceTokenResponseDto       deviceTokenResponseDto = convertDeviceToken(deviceToken);
        SuccessResponse<ResponseDto> successResponse        = SuccessResponse.builder().data(deviceTokenResponseDto).build();

        return new ResponseEntity<>(successResponse, HttpStatus.CREATED);
    }

    @RequestMapping(path = {"/devices/{deviceId}/tokens", "/v1/devices/{deviceId}/tokens"}, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SuccessResponse<ResponseDto>> list(@Min(value = 0, message = "page must be greater than or equal to 0")
                                                             @RequestParam(value = "page", defaultValue = "0") int page,
                                                             @Min(value = 1, message = "size must be greater than or equal to 1")
                                                             @Max(value = 25, message = "size must be less or equal than 25")
                                                             @RequestParam(value = "size", defaultValue = "10") int size,
                                                             @PathVariable("deviceId") String deviceId,
                                                             User authenticatedUser) throws DeviceNotFoundException
    {
        Page<DeviceToken> deviceTokens = this.deviceTokenService.list(deviceId, authenticatedUser.getId(), page, size);

        List<ResponseDto> deviceTokenResponseDtos = deviceTokens.getContent()
                                                                .stream()
                                                                .map(DeviceTokenController::convertDeviceToken)
                                                                .collect(Collectors.toList());

        PaginationResponseDto<ResponseDto> paginationResponseDto = PaginationResponseDto.builder()
                                                                                        .items(deviceTokenResponseDtos)
                                                                                        .totalElements(deviceTokens.getTotalElements())
                                                                                        .totalPages(deviceTokens.getTotalPages())
                                                                                        .build();

        SuccessResponse<ResponseDto> successResponse = SuccessResponse.builder()
                                                                      .data(paginationResponseDto)
                                                                      .build();

        return new ResponseEntity<>(successResponse, HttpStatus.OK);
    }

    @RequestMapping(path = {"/devices/{deviceId}/tokens/{deviceTokenId}", "/v1/devices/{deviceId}/tokens/{deviceTokenId}"}, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SuccessResponse<ResponseDto>> get(@PathVariable("deviceId") String deviceId,
                                                            @PathVariable("deviceTokenId") String deviceTokenId,
                                                            User authenticatedUser) throws DeviceNotFoundException, DeviceTokenNotFoundException
    {
        DeviceToken                  deviceToken            = this.deviceTokenService.get(deviceTokenId, deviceId, authenticatedUser.getId());
        DeviceTokenResponseDto       deviceTokenResponseDto = convertDeviceToken(deviceToken);
        SuccessResponse<ResponseDto> successResponse        = SuccessResponse.builder().data(deviceTokenResponseDto).build();

        return new ResponseEntity<>(successResponse, HttpStatus.OK);
    }

    @RequestMapping(path = {"/devices/{deviceId}/tokens/{deviceTokenId}", "/v1/devices/{deviceId}/tokens/{deviceTokenId}"}, method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SuccessResponse<ResponseDto>> delete(@PathVariable("deviceId") String deviceId,
                                                               @PathVariable("deviceTokenId") String deviceTokenId,
                                                               User authenticatedUser) throws DeviceNotFoundException, DeviceTokenNotFoundException
    {
        DeviceToken                  deviceToken            = this.deviceTokenService.delete(deviceTokenId, deviceId, authenticatedUser.getId());
        DeviceTokenResponseDto       deviceTokenResponseDto = convertDeviceToken(deviceToken);
        SuccessResponse<ResponseDto> successResponse        = SuccessResponse.builder().data(deviceTokenResponseDto).build();

        return new ResponseEntity<>(successResponse, HttpStatus.OK);
    }

    private static DeviceTokenResponseDto convertDeviceToken(DeviceToken deviceToken)
    {
        DeviceTokenResponseDto deviceTokenResponseDto = new DeviceTokenResponseDto();

        deviceTokenResponseDto.setId(deviceToken.getId());
        deviceTokenResponseDto.setToken(deviceToken.getToken());
        deviceTokenResponseDto.setCreatedAt(deviceToken.getCreatedAt());

        return deviceTokenResponseDto;
    }
}
