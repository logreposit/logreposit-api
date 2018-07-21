package com.logreposit.logrepositapi.rest.controllers;

import com.logreposit.logrepositapi.persistence.documents.User;
import com.logreposit.logrepositapi.rest.dtos.ResponseDto;
import com.logreposit.logrepositapi.rest.dtos.common.SuccessResponse;
import com.logreposit.logrepositapi.rest.dtos.response.AccountResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccountController
{
    @RequestMapping(path = "/account", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SuccessResponse<ResponseDto>> ingress(User authorizedUser)
    {
        AccountResponseDto           accountResponseDto = new AccountResponseDto(authorizedUser.getEmail());
        SuccessResponse<ResponseDto> successResponse    = SuccessResponse.builder().data(accountResponseDto).build();

        return new ResponseEntity<>(successResponse, HttpStatus.OK);
    }
}
