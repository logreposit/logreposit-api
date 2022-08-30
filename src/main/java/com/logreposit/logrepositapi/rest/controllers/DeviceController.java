package com.logreposit.logrepositapi.rest.controllers;

import com.logreposit.logrepositapi.persistence.documents.Device;
import com.logreposit.logrepositapi.persistence.documents.User;
import com.logreposit.logrepositapi.rest.dtos.ResponseDto;
import com.logreposit.logrepositapi.rest.dtos.common.SuccessResponse;
import com.logreposit.logrepositapi.rest.dtos.request.DeviceCreationRequestDto;
import com.logreposit.logrepositapi.rest.dtos.response.DeviceResponseDto;
import com.logreposit.logrepositapi.rest.dtos.response.PaginationResponseDto;
import com.logreposit.logrepositapi.rest.mappers.DeviceDefinitionMapper;
import com.logreposit.logrepositapi.services.device.DeviceNotFoundException;
import com.logreposit.logrepositapi.services.device.DeviceService;
import com.logreposit.logrepositapi.services.device.DeviceServiceException;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
public class DeviceController {
  private static final Logger logger = LoggerFactory.getLogger(DeviceController.class);

  private final DeviceService deviceService;

  public DeviceController(DeviceService deviceService) {
    this.deviceService = deviceService;
  }

  @PostMapping(path = "/v1/devices")
  public ResponseEntity<SuccessResponse<ResponseDto>> create(
      @Valid @RequestBody DeviceCreationRequestDto deviceCreationRequestDto, User authenticatedUser)
      throws DeviceServiceException {
    final var device = buildDevice(deviceCreationRequestDto, authenticatedUser.getId());
    final var createdDevice = this.deviceService.create(device, authenticatedUser.getEmail());
    final var deviceResponseDto = convertDevice(createdDevice);

    final var successResponse = SuccessResponse.builder().data(deviceResponseDto).build();

    logger.info("Successfully created device: {}", createdDevice);

    return new ResponseEntity<>(successResponse, HttpStatus.CREATED);
  }

  @GetMapping(path = "/v1/devices")
  public ResponseEntity<SuccessResponse<ResponseDto>> list(
      @Min(value = 0, message = "page must be greater than or equal to 0")
          @RequestParam(value = "page", defaultValue = "0")
          int page,
      @Min(value = 1, message = "size must be greater than or equal to 1")
          @Max(value = 25, message = "size must be less or equal than 25")
          @RequestParam(value = "size", defaultValue = "10")
          int size,
      User authenticatedUser) {
    final var devices = this.deviceService.list(authenticatedUser.getId(), page, size);

    final var deviceResponseDtos =
        devices.getContent().stream()
            .map(DeviceController::convertDevice)
            .collect(Collectors.toList());

    final var paginationResponseDto =
        PaginationResponseDto.<DeviceResponseDto>builder()
            .items(deviceResponseDtos)
            .totalElements(devices.getTotalElements())
            .totalPages(devices.getTotalPages())
            .build();

    final var successResponse = SuccessResponse.builder().data(paginationResponseDto).build();

    return new ResponseEntity<>(successResponse, HttpStatus.OK);
  }

  @GetMapping(path = "/v1/devices/{id}")
  public ResponseEntity<SuccessResponse<ResponseDto>> get(
      @PathVariable("id") String id, User authenticatedUser) throws DeviceNotFoundException {
    final var device = this.deviceService.get(id, authenticatedUser.getId());
    final var deviceResponseDto = convertDevice(device);

    final var successResponse = SuccessResponse.builder().data(deviceResponseDto).build();

    return new ResponseEntity<>(successResponse, HttpStatus.OK);
  }

  @DeleteMapping(path = "/v1/devices/{id}")
  public ResponseEntity<SuccessResponse<ResponseDto>> delete(
      @PathVariable("id") String id, User authenticatedUser) throws DeviceNotFoundException {
    final var device = this.deviceService.delete(id, authenticatedUser.getId());
    final var deviceResponseDto = convertDevice(device);

    final var successResponse = SuccessResponse.builder().data(deviceResponseDto).build();

    return new ResponseEntity<>(successResponse, HttpStatus.OK);
  }

  private static Device buildDevice(
      DeviceCreationRequestDto deviceCreationRequestDto, String userId) {
    final var device = new Device();

    device.setName(deviceCreationRequestDto.getName());
    device.setUserId(userId);

    return device;
  }

  private static DeviceResponseDto convertDevice(Device device) {
    final var deviceDefinitionDto =
        Optional.ofNullable(device.getDefinition()).map(DeviceDefinitionMapper::toDto).orElse(null);

    return new DeviceResponseDto(device.getId(), device.getName(), deviceDefinitionDto);
  }
}
