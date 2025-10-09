package com.logreposit.logrepositapi.communication.messaging.handler;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

// TODO DoM: Make application-mode conditional?
@Service
public class InfluxMessageHandler extends AbstractMessageHandler {
  private static final Logger logger = LoggerFactory.getLogger(InfluxMessageHandler.class);

  public InfluxMessageHandler() {
    super(Map.of()); // TODO DoM

    logger.info("TODO DoM DEBUG: instantiating InfluxMessageHandler ...");
  }
}
