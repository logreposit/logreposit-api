package com.logreposit.logrepositapi.rest.controllers;

import com.logreposit.logrepositapi.persistence.documents.MqttCredential;
import com.logreposit.logrepositapi.persistence.documents.MqttRole;
import com.logreposit.logrepositapi.persistence.documents.User;
import com.logreposit.logrepositapi.rest.dtos.ResponseDto;
import com.logreposit.logrepositapi.rest.dtos.common.SuccessResponse;
import com.logreposit.logrepositapi.rest.dtos.request.MqttCredentialRequestDto;
import com.logreposit.logrepositapi.rest.dtos.response.MqttCredentialResponseDto;
import com.logreposit.logrepositapi.rest.dtos.response.PaginationResponseDto;
import com.logreposit.logrepositapi.services.mqtt.MqttCredentialNotFoundException;
import com.logreposit.logrepositapi.services.mqtt.MqttCredentialService;
import com.logreposit.logrepositapi.services.mqtt.MqttCredentialServiceException;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
public class MqttCredentialController {
  private final MqttCredentialService mqttCredentialService;

  public MqttCredentialController(MqttCredentialService mqttCredentialService) {
    this.mqttCredentialService = mqttCredentialService;
  }

  @PostMapping(path = "/v1/account/mqtt-credentials")
  public ResponseEntity<SuccessResponse<ResponseDto>> create(
      @Valid @RequestBody MqttCredentialRequestDto mqttCredentialRequestDto, User authenticatedUser)
      throws MqttCredentialServiceException {
    final var mqttCredential =
        this.mqttCredentialService.create(
            authenticatedUser.getId(),
            mqttCredentialRequestDto.getDescription(),
            List.of(MqttRole.ACCOUNT_DEVICE_DATA_READ));

    final var mqttCredentialResponseDto = convertMqttCredential(mqttCredential);

    final var successResponse = SuccessResponse.builder().data(mqttCredentialResponseDto).build();

    return new ResponseEntity<>(successResponse, HttpStatus.CREATED);
  }

  @GetMapping(path = "/v1/account/mqtt-credentials")
  public ResponseEntity<SuccessResponse<ResponseDto>> list(
      @Min(value = 0, message = "page must be greater than or equal to 0")
          @RequestParam(value = "page", defaultValue = "0")
          int page,
      @Min(value = 1, message = "size must be greater than or equal to 1")
          @Max(value = 25, message = "size must be less or equal than 25")
          @RequestParam(value = "size", defaultValue = "10")
          int size,
      User authenticatedUser) {
    final var mqttCredentials =
        this.mqttCredentialService.list(authenticatedUser.getId(), page, size);

    final var responseDtos =
        mqttCredentials.getContent().stream()
            .map(MqttCredentialController::convertMqttCredential)
            .toList();

    final var paginationResponseDto =
        PaginationResponseDto.<MqttCredentialResponseDto>builder()
            .items(responseDtos)
            .totalElements(mqttCredentials.getTotalElements())
            .totalPages(mqttCredentials.getTotalPages())
            .build();

    final var successResponse = SuccessResponse.builder().data(paginationResponseDto).build();

    return new ResponseEntity<>(successResponse, HttpStatus.OK);
  }

  @RequestMapping(
      path = "/v1/account/mqtt-credentials/{id}",
      method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<SuccessResponse<ResponseDto>> get(
      @PathVariable("id") String id, User authenticatedUser)
      throws MqttCredentialNotFoundException {
    final var mqttCredential = this.mqttCredentialService.get(id, authenticatedUser.getId());
    final var responseDto = convertMqttCredential(mqttCredential);
    final var successResponse = SuccessResponse.builder().data(responseDto).build();

    return new ResponseEntity<>(successResponse, HttpStatus.OK);
  }

  @RequestMapping(
      path = "/v1/account/mqtt-credentials/{id}",
      method = RequestMethod.DELETE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<SuccessResponse<ResponseDto>> delete(
      @PathVariable("id") String id, User authenticatedUser) throws MqttCredentialServiceException {
    final var mqttCredential = this.mqttCredentialService.delete(id, authenticatedUser.getId());
    final var responseDto = convertMqttCredential(mqttCredential);
    final var successResponse = SuccessResponse.builder().data(responseDto).build();

    return new ResponseEntity<>(successResponse, HttpStatus.OK);
  }

  private static MqttCredentialResponseDto convertMqttCredential(MqttCredential mqttCredential) {
    final var roles = mqttCredential.getRoles().stream().map(Enum::toString).toList();
    return new MqttCredentialResponseDto(
        mqttCredential.getId(),
        mqttCredential.getUsername(),
        mqttCredential.getPassword(),
        mqttCredential.getDescription(),
        roles,
        mqttCredential.getCreatedAt());
  }
}
