package com.logreposit.logrepositapi.rest.controllers;

import com.logreposit.logrepositapi.persistence.documents.Device;
import com.logreposit.logrepositapi.persistence.documents.User;
import com.logreposit.logrepositapi.rest.dtos.ResponseDto;
import com.logreposit.logrepositapi.rest.dtos.common.SuccessResponse;
import com.logreposit.logrepositapi.rest.dtos.request.DeviceCreationRequestDto;
import com.logreposit.logrepositapi.rest.dtos.response.DeviceResponseDto;
import com.logreposit.logrepositapi.rest.dtos.response.PaginationResponseDto;
import com.logreposit.logrepositapi.rest.dtos.shared.definition.DeviceDefinitionDto;
import com.logreposit.logrepositapi.rest.mappers.DeviceDefinitionMapper;
import com.logreposit.logrepositapi.services.device.DeviceNotFoundException;
import com.logreposit.logrepositapi.services.device.DeviceService;
import com.logreposit.logrepositapi.services.device.DeviceServiceException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
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
    Device device = buildDevice(deviceCreationRequestDto, authenticatedUser.getId());
    Device createdDevice = this.deviceService.create(device, authenticatedUser.getEmail());
    DeviceResponseDto deviceResponseDto = convertDevice(createdDevice);
    SuccessResponse<ResponseDto> successResponse =
        SuccessResponse.builder().data(deviceResponseDto).build();

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
    Page<Device> devices = this.deviceService.list(authenticatedUser.getId(), page, size);

    List<ResponseDto> deviceResponseDtos =
        devices.getContent().stream()
            .map(DeviceController::convertDevice)
            .collect(Collectors.toList());

    PaginationResponseDto<ResponseDto> paginationResponseDto =
        PaginationResponseDto.builder()
            .items(deviceResponseDtos)
            .totalElements(devices.getTotalElements())
            .totalPages(devices.getTotalPages())
            .build();

    SuccessResponse<ResponseDto> successResponse =
        SuccessResponse.builder().data(paginationResponseDto).build();

    return new ResponseEntity<>(successResponse, HttpStatus.OK);
  }

  @GetMapping(path = "/v1/devices/{id}")
  public ResponseEntity<SuccessResponse<ResponseDto>> get(
      @PathVariable("id") String id, User authenticatedUser) throws DeviceNotFoundException {
    Device device = this.deviceService.get(id, authenticatedUser.getId());
    DeviceResponseDto deviceResponseDto = convertDevice(device);
    SuccessResponse<ResponseDto> successResponse =
        SuccessResponse.builder().data(deviceResponseDto).build();

    return new ResponseEntity<>(successResponse, HttpStatus.OK);
  }

  @DeleteMapping(path = "/v1/devices/{id}")
  public ResponseEntity<SuccessResponse<ResponseDto>> delete(
      @PathVariable("id") String id, User authenticatedUser) throws DeviceNotFoundException {
    Device device = this.deviceService.delete(id, authenticatedUser.getId());
    DeviceResponseDto deviceResponseDto = convertDevice(device);
    SuccessResponse<ResponseDto> successResponse =
        SuccessResponse.builder().data(deviceResponseDto).build();

    return new ResponseEntity<>(successResponse, HttpStatus.OK);
  }

  private static Device buildDevice(
      DeviceCreationRequestDto deviceCreationRequestDto, String userId) {
    Device device = new Device();

    device.setName(deviceCreationRequestDto.getName());
    device.setUserId(userId);

    return device;
  }

  private static DeviceResponseDto convertDevice(Device device) {
    DeviceDefinitionDto deviceDefinitionDto =
        Optional.ofNullable(device.getDefinition()).map(DeviceDefinitionMapper::toDto).orElse(null);

    DeviceResponseDto deviceResponseDto =
        new DeviceResponseDto(device.getId(), device.getName(), deviceDefinitionDto);

    return deviceResponseDto;
  }
}
