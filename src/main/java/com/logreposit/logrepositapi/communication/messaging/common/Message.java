package com.logreposit.logrepositapi.communication.messaging.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Message
{
    private String          id;
    private Date            date;
    private String          type;
    private MessageMetaData metaData;
    private String          payload;
}
