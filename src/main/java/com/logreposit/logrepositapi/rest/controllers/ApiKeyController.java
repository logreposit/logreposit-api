package com.logreposit.logrepositapi.rest.controllers;

import com.logreposit.logrepositapi.persistence.documents.ApiKey;
import com.logreposit.logrepositapi.persistence.documents.User;
import com.logreposit.logrepositapi.rest.dtos.ResponseDto;
import com.logreposit.logrepositapi.rest.dtos.common.SuccessResponse;
import com.logreposit.logrepositapi.rest.dtos.response.ApiKeyResponseDto;
import com.logreposit.logrepositapi.rest.dtos.response.PaginationResponseDto;
import com.logreposit.logrepositapi.services.apikey.ApiKeyService;
import com.logreposit.logrepositapi.services.common.ApiKeyNotFoundException;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
public class ApiKeyController {
  private final ApiKeyService apiKeyService;

  public ApiKeyController(ApiKeyService apiKeyService) {
    this.apiKeyService = apiKeyService;
  }

  @PostMapping(path = "/v1/account/api-keys")
  public ResponseEntity<SuccessResponse<ResponseDto>> create(User authenticatedUser) {
    final var apiKey = this.apiKeyService.create(authenticatedUser.getId());
    final var apiKeyResponseDto = convertApiKey(apiKey);

    final var successResponse = SuccessResponse.builder().data(apiKeyResponseDto).build();

    return new ResponseEntity<>(successResponse, HttpStatus.CREATED);
  }

  @GetMapping(path = "/v1/account/api-keys")
  public ResponseEntity<SuccessResponse<ResponseDto>> list(
      @Min(value = 0, message = "page must be greater than or equal to 0")
          @RequestParam(value = "page", defaultValue = "0")
          int page,
      @Min(value = 1, message = "size must be greater than or equal to 1")
          @Max(value = 25, message = "size must be less or equal than 25")
          @RequestParam(value = "size", defaultValue = "10")
          int size,
      User authenticatedUser) {
    final var apiKeys = this.apiKeyService.list(authenticatedUser.getId(), page, size);

    final var apiKeyResponseDtos =
        apiKeys.getContent().stream()
            .map(ApiKeyController::convertApiKey)
            .collect(Collectors.toList());

    final var paginationResponseDto =
        PaginationResponseDto.<ApiKeyResponseDto>builder()
            .items(apiKeyResponseDtos)
            .totalElements(apiKeys.getTotalElements())
            .totalPages(apiKeys.getTotalPages())
            .build();

    final var successResponse = SuccessResponse.builder().data(paginationResponseDto).build();

    return new ResponseEntity<>(successResponse, HttpStatus.OK);
  }

  @RequestMapping(
      path = "/v1/account/api-keys/{id}",
      method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<SuccessResponse<ResponseDto>> get(
      @PathVariable("id") String id, User authenticatedUser) throws ApiKeyNotFoundException {
    final var apiKey = this.apiKeyService.get(id, authenticatedUser.getId());
    final var apiKeyResponseDto = convertApiKey(apiKey);

    final var successResponse = SuccessResponse.builder().data(apiKeyResponseDto).build();

    return new ResponseEntity<>(successResponse, HttpStatus.OK);
  }

  @RequestMapping(
      path = "/v1/account/api-keys/{id}",
      method = RequestMethod.DELETE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<SuccessResponse<ResponseDto>> delete(
      @PathVariable("id") String id, User authenticatedUser) throws ApiKeyNotFoundException {
    final var apiKey = this.apiKeyService.delete(id, authenticatedUser.getId());
    final var apiKeyResponseDto = convertApiKey(apiKey);

    final var successResponse = SuccessResponse.builder().data(apiKeyResponseDto).build();

    return new ResponseEntity<>(successResponse, HttpStatus.OK);
  }

  private static ApiKeyResponseDto convertApiKey(ApiKey apiKey) {
    return new ApiKeyResponseDto(apiKey.getId(), apiKey.getKey(), apiKey.getCreatedAt());
  }
}
