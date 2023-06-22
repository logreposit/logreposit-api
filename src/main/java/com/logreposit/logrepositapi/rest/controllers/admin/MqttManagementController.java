package com.logreposit.logrepositapi.rest.controllers.admin;

import com.logreposit.logrepositapi.rest.dtos.ResponseDto;
import com.logreposit.logrepositapi.rest.dtos.common.SuccessResponse;
import com.logreposit.logrepositapi.services.mqtt.MqttCredentialService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
public class MqttManagementController {
  private final MqttCredentialService mqttCredentialService;

  public MqttManagementController(MqttCredentialService mqttCredentialService) {
    this.mqttCredentialService = mqttCredentialService;
  }

  @PostMapping(path = "/v1/admin/mqtt-credentials/actions/sync-all")
  public ResponseEntity<SuccessResponse<ResponseDto>> sync() {
    this.mqttCredentialService.syncAll();

    final var successResponse = SuccessResponse.builder().build();

    return new ResponseEntity<>(successResponse, HttpStatus.OK);
  }
}
