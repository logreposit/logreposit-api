package com.logreposit.logrepositapi.services.influxdb.batchpoints.generic;

import com.logreposit.logrepositapi.rest.dtos.request.ingress.FieldDto;
import com.logreposit.logrepositapi.rest.dtos.request.ingress.FloatFieldDto;
import com.logreposit.logrepositapi.rest.dtos.request.ingress.IntegerFieldDto;
import com.logreposit.logrepositapi.rest.dtos.request.ingress.ReadingDto;
import com.logreposit.logrepositapi.rest.dtos.request.ingress.StringFieldDto;
import com.logreposit.logrepositapi.rest.dtos.request.ingress.TagDto;
import java.util.List;
import java.util.Map;
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

    BatchPoints.Builder batchPointsBuilder = BatchPoints.database(deviceId);

    for (ReadingDto reading : readings) {
      batchPointsBuilder.point(this.createPoint(reading));
    }

    BatchPoints batchPoints = batchPointsBuilder.build();

    return batchPoints;
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

    final Map<String, String> tags =
        reading.getTags().stream().collect(Collectors.toMap(TagDto::getName, TagDto::getValue));

    Point.Builder pointBuilder =
        Point.measurement(reading.getMeasurement())
            .time(unixTimestamp, TimeUnit.MILLISECONDS)
            .tag(tags);

    for (FieldDto field : reading.getFields()) {
      if (field instanceof FloatFieldDto) {
        pointBuilder.addField(field.getName(), ((FloatFieldDto) field).getValue());
      } else if (field instanceof IntegerFieldDto) {
        pointBuilder.addField(field.getName(), ((IntegerFieldDto) field).getValue());
      } else if (field instanceof StringFieldDto) {
        pointBuilder.addField(field.getName(), ((StringFieldDto) field).getValue());
      } else {
        throw new GenericLogdataBatchPointsFactoryException(
            String.format("Got unknown FieldDto with DataType '%s'", field.getDatatype()));
      }
    }

    return pointBuilder.build();
  }
}
