package com.logreposit.logrepositapi.rest.controllers.admin;

import com.logreposit.logrepositapi.persistence.documents.User;
import com.logreposit.logrepositapi.rest.dtos.ResponseDto;
import com.logreposit.logrepositapi.rest.dtos.common.SuccessResponse;
import com.logreposit.logrepositapi.rest.dtos.request.UserCreationRequestDto;
import com.logreposit.logrepositapi.rest.dtos.response.PaginationResponseDto;
import com.logreposit.logrepositapi.rest.dtos.response.UserResponseDto;
import com.logreposit.logrepositapi.services.user.UserAlreadyExistentException;
import com.logreposit.logrepositapi.services.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class UserManagementController
{
    private static final Logger logger = LoggerFactory.getLogger(UserManagementController.class);

    private final UserService userService;

    public UserManagementController(UserService userService)
    {
        this.userService = userService;
    }

    @RequestMapping(path = "/admin/users", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SuccessResponse<ResponseDto>> create(@Valid @RequestBody UserCreationRequestDto userCreationRequestDto) throws UserAlreadyExistentException
    {
        User                         userToCreate    = convertUser(userCreationRequestDto);
        User                         createdUser     = this.userService.create(userToCreate);
        UserResponseDto              userResponseDto = convertUser(createdUser);

        SuccessResponse<ResponseDto> successResponse = SuccessResponse.builder()
                                                                      .data(userResponseDto)
                                                                      .build();

        return new ResponseEntity<>(successResponse, HttpStatus.CREATED);
    }

    @RequestMapping(path = "/admin/users", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SuccessResponse<ResponseDto>> list(@Min(value = 0, message = "page must be greater than or equal to 0")
                                             @RequestParam(value = "page", defaultValue = "0") int page,
                                             @Min(value = 1, message = "size must be greater than or equal to 1")
                                             @Max(value = 25, message = "size must be less or equal than 25")
                                             @RequestParam(value = "page", defaultValue = "10") int size,
                                             User authenticatedUser)
    {
        logger.info(authenticatedUser.toString());

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
        user.setRoles(Collections.singletonList("USER"));

        return user;
    }

    private static UserResponseDto convertUser(User user)
    {
        UserResponseDto userResponseDto = new UserResponseDto();

        userResponseDto.setId(user.getId());
        userResponseDto.setEmail(user.getEmail());

        return userResponseDto;
    }
}
