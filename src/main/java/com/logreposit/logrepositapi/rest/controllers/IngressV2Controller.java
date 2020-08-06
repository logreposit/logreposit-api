package com.logreposit.logrepositapi.rest.controllers;

import com.logreposit.logrepositapi.persistence.documents.Device;
import com.logreposit.logrepositapi.persistence.documents.definition.DeviceDefinition;
import com.logreposit.logrepositapi.rest.dtos.ResponseDto;
import com.logreposit.logrepositapi.rest.dtos.common.SuccessResponse;
import com.logreposit.logrepositapi.rest.dtos.request.IngressRequestDto;
import com.logreposit.logrepositapi.rest.dtos.request.ingress.IngressV2RequestDto;
import com.logreposit.logrepositapi.rest.dtos.response.IngressResponseDto;
import com.logreposit.logrepositapi.rest.dtos.shared.definition.DeviceDefinitionDto;
import com.logreposit.logrepositapi.rest.mappers.DeviceDefinitionMapper;
import com.logreposit.logrepositapi.services.device.DeviceService;
import com.logreposit.logrepositapi.services.device.DeviceServiceException;
import com.logreposit.logrepositapi.services.ingress.IngressServiceException;
import com.logreposit.logrepositapi.utils.duration.DurationCalculator;
import com.logreposit.logrepositapi.utils.duration.DurationCalculatorException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Date;

@RestController
@Validated
public class IngressV2Controller
{
    private final DurationCalculator durationCalculator;
    private final DeviceService deviceService;

    public IngressV2Controller(DurationCalculator durationCalculator,
                               DeviceService deviceService)
    {
        this.durationCalculator = durationCalculator;
        this.deviceService = deviceService;
    }

    @RequestMapping(path = "/v2/ingress/data", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SuccessResponse<ResponseDto>> ingressData(Device device, @RequestBody @Valid IngressV2RequestDto ingressRequestDto)
            throws IngressServiceException, DurationCalculatorException
    {
        Date start = new Date();

        // this.ingressService.processData(device, ingressRequestDto.getDeviceType(), ingressRequestDto.getData());
        // TODO!

        long delta = this.durationCalculator.getDuration(start, new Date());

        return new ResponseEntity<>(buildResponse(delta), HttpStatus.ACCEPTED);
    }

    @RequestMapping(path = "/v2/ingress/definition", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SuccessResponse<ResponseDto>> ingressDefinition(Device device, @RequestBody @Valid DeviceDefinitionDto deviceDefinitionDto)
            throws DurationCalculatorException, DeviceServiceException
    {
        Date start = new Date();

        DeviceDefinition deviceDefinition = DeviceDefinitionMapper.toEntity(deviceDefinitionDto);

        this.deviceService.updateDefinition(device.getId(), deviceDefinition);

        long delta = this.durationCalculator.getDuration(start, new Date());

        return new ResponseEntity<>(buildResponse(delta), HttpStatus.ACCEPTED);
    }

    private static SuccessResponse<ResponseDto> buildResponse(long delta)
    {
        String                       message            = String.format("Data was accepted for processing in %d milliseconds.", delta);
        IngressResponseDto           ingressResponseDto = new IngressResponseDto(message);
        SuccessResponse<ResponseDto> successResponse    = SuccessResponse.builder().data(ingressResponseDto).build();

        return successResponse;
    }
}
