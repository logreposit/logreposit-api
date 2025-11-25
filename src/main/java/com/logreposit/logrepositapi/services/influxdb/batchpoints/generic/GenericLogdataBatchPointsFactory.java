package com.logreposit.logrepositapi.services.influxdb.batchpoints.generic;

import com.logreposit.logrepositapi.rest.dtos.request.ingress.FieldDto;
import com.logreposit.logrepositapi.rest.dtos.request.ingress.FloatFieldDto;
import com.logreposit.logrepositapi.rest.dtos.request.ingress.IntegerFieldDto;
import com.logreposit.logrepositapi.rest.dtos.request.ingress.ReadingDto;
import com.logreposit.logrepositapi.rest.dtos.request.ingress.StringFieldDto;
import com.logreposit.logrepositapi.rest.dtos.request.ingress.TagDto;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class GenericLogdataBatchPointsFactory {
  private static final Logger logger =
      LoggerFactory.getLogger(GenericLogdataBatchPointsFactory.class);

  public BatchPoints createBatchPoints(String deviceId, List<ReadingDto> readings)
      throws GenericLogdataBatchPointsFactoryException {
    checkIfInputIsValidOtherwiseThrowException(deviceId);

    final var batchPointsBuilder = BatchPoints.database(deviceId);

    for (ReadingDto reading : readings) {
      batchPointsBuilder.point(this.createPoint(reading));
    }

    return batchPointsBuilder.build();
  }

  private static void checkIfInputIsValidOtherwiseThrowException(String deviceId)
      throws GenericLogdataBatchPointsFactoryException {
    if (StringUtils.isBlank(deviceId)) {
      logger.error("deviceId is blank!");
      throw new GenericLogdataBatchPointsFactoryException("deviceId is blank!");
    }
  }

  private Point createPoint(ReadingDto reading) throws GenericLogdataBatchPointsFactoryException {
    long unixTimestamp = reading.getDate().toEpochMilli();

    final var tags =
        reading.getTags().stream().collect(Collectors.toMap(TagDto::getName, TagDto::getValue));

    final var pointBuilder =
        Point.measurement(reading.getMeasurement())
            .time(unixTimestamp, TimeUnit.MILLISECONDS)
            .tag(tags);

    for (FieldDto field : reading.getFields()) {
      switch (field) {
        case FloatFieldDto floatFieldDto ->
            pointBuilder.addField(field.getName(), floatFieldDto.getValue());
        case IntegerFieldDto integerFieldDto ->
            pointBuilder.addField(field.getName(), integerFieldDto.getValue());
        case StringFieldDto stringFieldDto ->
            pointBuilder.addField(field.getName(), stringFieldDto.getValue());
        default ->
            throw new GenericLogdataBatchPointsFactoryException(
                String.format("Got unknown FieldDto with DataType '%s'", field.getDatatype()));
      }
    }

    return pointBuilder.build();
  }
}
