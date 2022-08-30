package com.logreposit.logrepositapi.communication.messaging.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Date;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Message {
  private String id;
  private Date date;
  private String type;
  private MessageMetaData metaData;
  private String payload;
}
