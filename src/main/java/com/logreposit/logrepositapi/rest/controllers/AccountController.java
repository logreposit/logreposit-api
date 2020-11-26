package com.logreposit.logrepositapi.rest.controllers;

import com.logreposit.logrepositapi.persistence.documents.User;
import com.logreposit.logrepositapi.rest.dtos.ResponseDto;
import com.logreposit.logrepositapi.rest.dtos.common.SuccessResponse;
import com.logreposit.logrepositapi.rest.dtos.response.AccountResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
public class AccountController
{
    @GetMapping(path = {"/account", "/v1/account"})
    public ResponseEntity<SuccessResponse<ResponseDto>> get(User authenticatedUser)
    {
        AccountResponseDto           accountResponseDto = new AccountResponseDto(authenticatedUser.getEmail());
        SuccessResponse<ResponseDto> successResponse    = SuccessResponse.builder().data(accountResponseDto).build();

        return new ResponseEntity<>(successResponse, HttpStatus.OK);
    }
}
