package com.logreposit.logrepositapi.rest.controllers;

import com.logreposit.logrepositapi.persistence.documents.ApiKey;
import com.logreposit.logrepositapi.persistence.documents.User;
import com.logreposit.logrepositapi.rest.dtos.ResponseDto;
import com.logreposit.logrepositapi.rest.dtos.common.SuccessResponse;
import com.logreposit.logrepositapi.rest.dtos.response.ApiKeyResponseDto;
import com.logreposit.logrepositapi.rest.dtos.response.PaginationResponseDto;
import com.logreposit.logrepositapi.services.apikey.ApiKeyService;
import com.logreposit.logrepositapi.services.common.ApiKeyNotFoundException;
import org.springframework.data.domain.Page;
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

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Validated
public class ApiKeyController
{
    private final ApiKeyService apiKeyService;

    public ApiKeyController(ApiKeyService apiKeyService)
    {
        this.apiKeyService = apiKeyService;
    }

    @PostMapping(path = {"/account/api-keys", "/v1/account/api-keys"})
    public ResponseEntity<SuccessResponse<ResponseDto>> create(User authenticatedUser)
    {
        ApiKey                       apiKey            = this.apiKeyService.create(authenticatedUser.getId());
        ApiKeyResponseDto            apiKeyResponseDto = convertApiKey(apiKey);
        SuccessResponse<ResponseDto> successResponse   = SuccessResponse.builder().data(apiKeyResponseDto).build();

        return new ResponseEntity<>(successResponse, HttpStatus.CREATED);
    }

    @GetMapping(path = {"/account/api-keys", "/v1/account/api-keys"})
    public ResponseEntity<SuccessResponse<ResponseDto>> list(@Min(value = 0, message = "page must be greater than or equal to 0")
                                                             @RequestParam(value = "page", defaultValue = "0") int page,
                                                             @Min(value = 1, message = "size must be greater than or equal to 1")
                                                             @Max(value = 25, message = "size must be less or equal than 25")
                                                             @RequestParam(value = "size", defaultValue = "10") int size,
                                                             User authenticatedUser)
    {
        Page<ApiKey> apiKeys = this.apiKeyService.list(authenticatedUser.getId(), page, size);

        List<ResponseDto> apiKeyResponseDtos = apiKeys.getContent()
                                                      .stream()
                                                      .map(ApiKeyController::convertApiKey)
                                                      .collect(Collectors.toList());

        PaginationResponseDto<ResponseDto> paginationResponseDto = PaginationResponseDto.builder()
                                                                                        .items(apiKeyResponseDtos)
                                                                                        .totalElements(apiKeys.getTotalElements())
                                                                                        .totalPages(apiKeys.getTotalPages())
                                                                                        .build();

        SuccessResponse<ResponseDto> successResponse = SuccessResponse.builder()
                                                                      .data(paginationResponseDto)
                                                                      .build();

        return new ResponseEntity<>(successResponse, HttpStatus.OK);
    }

    @RequestMapping(path = {"/account/api-keys/{id}", "/v1/account/api-keys/{id}"}, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SuccessResponse<ResponseDto>> get(@PathVariable("id") String id,
                                                            User authenticatedUser) throws ApiKeyNotFoundException
    {
        ApiKey                       apiKey            = this.apiKeyService.get(id, authenticatedUser.getId());
        ApiKeyResponseDto            apiKeyResponseDto = convertApiKey(apiKey);
        SuccessResponse<ResponseDto> successResponse   = SuccessResponse.builder().data(apiKeyResponseDto).build();

        return new ResponseEntity<>(successResponse, HttpStatus.OK);
    }

    @RequestMapping(path = {"/account/api-keys/{id}", "/v1/account/api-keys/{id}"}, method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SuccessResponse<ResponseDto>> delete(@PathVariable("id") String id,
                                                               User authenticatedUser) throws ApiKeyNotFoundException
    {
        ApiKey                       apiKey            = this.apiKeyService.delete(id, authenticatedUser.getId());
        ApiKeyResponseDto            apiKeyResponseDto = convertApiKey(apiKey);
        SuccessResponse<ResponseDto> successResponse   = SuccessResponse.builder().data(apiKeyResponseDto).build();

        return new ResponseEntity<>(successResponse, HttpStatus.OK);
    }

    private static ApiKeyResponseDto convertApiKey(ApiKey apiKey)
    {
        ApiKeyResponseDto apiKeyResponseDto = new ApiKeyResponseDto();

        apiKeyResponseDto.setId(apiKey.getId());
        apiKeyResponseDto.setKey(apiKey.getKey());
        apiKeyResponseDto.setCreatedAt(apiKey.getCreatedAt());

        return apiKeyResponseDto;
    }
}
