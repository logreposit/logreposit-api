package com.logreposit.logrepositapi.rest.controllers.admin;

import com.logreposit.logrepositapi.persistence.documents.ApiKey;
import com.logreposit.logrepositapi.persistence.documents.User;
import com.logreposit.logrepositapi.rest.dtos.ResponseDto;
import com.logreposit.logrepositapi.rest.dtos.common.SuccessResponse;
import com.logreposit.logrepositapi.rest.dtos.request.UserCreationRequestDto;
import com.logreposit.logrepositapi.rest.dtos.response.PaginationResponseDto;
import com.logreposit.logrepositapi.rest.dtos.response.UserCreatedResponseDto;
import com.logreposit.logrepositapi.rest.dtos.response.UserResponseDto;
import com.logreposit.logrepositapi.rest.security.UserRoles;
import com.logreposit.logrepositapi.services.user.CreatedUser;
import com.logreposit.logrepositapi.services.user.UserService;
import com.logreposit.logrepositapi.services.user.UserServiceException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Validated
public class UserManagementController
{
    private final UserService userService;

    public UserManagementController(UserService userService)
    {
        this.userService = userService;
    }

    @PostMapping(path = {"/admin/users", "/v1/admin/users"})
    public ResponseEntity<SuccessResponse<ResponseDto>> create(@Valid @RequestBody UserCreationRequestDto userCreationRequestDto) throws UserServiceException
    {
        User                   userToCreate           = convertUser(userCreationRequestDto);
        CreatedUser            createdUser            = this.userService.create(userToCreate);
        UserCreatedResponseDto userCreatedResponseDto = convertUser(createdUser.getUser(), createdUser.getApiKey());

        SuccessResponse<ResponseDto> successResponse = SuccessResponse.builder()
                                                                      .data(userCreatedResponseDto)
                                                                      .build();

        return new ResponseEntity<>(successResponse, HttpStatus.CREATED);
    }

    @GetMapping(path = {"/admin/users", "/v1/admin/users"})
    public ResponseEntity<SuccessResponse<ResponseDto>> list(@Min(value = 0, message = "page must be greater than or equal to 0")
                                                             @RequestParam(value = "page", defaultValue = "0") int page,
                                                             @Min(value = 1, message = "size must be greater than or equal to 1")
                                                             @Max(value = 25, message = "size must be less or equal than 25")
                                                             @RequestParam(value = "size", defaultValue = "10") int size)
    {
        Page<User> users = this.userService.list(page, size);

        List<ResponseDto> userResponseDtos = users.getContent()
                                                  .stream()
                                                  .map(UserManagementController::convertUser)
                                                  .collect(Collectors.toList());

        PaginationResponseDto<ResponseDto> paginationResponseDto = PaginationResponseDto.builder()
                                                                                        .items(userResponseDtos)
                                                                                        .totalElements(users.getTotalElements())
                                                                                        .totalPages(users.getTotalPages())
                                                                                        .build();

        SuccessResponse<ResponseDto> successResponse = SuccessResponse.builder()
                                                                      .data(paginationResponseDto)
                                                                      .build();

        return new ResponseEntity<>(successResponse, HttpStatus.OK);
    }

    private static User convertUser(UserCreationRequestDto userCreationRequestDto)
    {
        User user = new User();

        user.setEmail(userCreationRequestDto.getEmail());
        user.setPassword(userCreationRequestDto.getPassword());
        user.setRoles(Collections.singletonList(UserRoles.USER));

        return user;
    }

    private static UserResponseDto convertUser(User user)
    {
        return new UserResponseDto(
                user.getId(),
                user.getEmail(),
                user.getRoles()
        );
    }

    private static UserCreatedResponseDto convertUser(User user, ApiKey apiKey)
    {
        return new UserCreatedResponseDto(
                user.getId(),
                user.getEmail(),
                user.getRoles(),
                apiKey.getKey()
        );
    }
}
