package com.logreposit.logrepositapi.services.influxdb;

import com.logreposit.logrepositapi.configuration.ApplicationConfiguration;
import com.logreposit.logrepositapi.configuration.conditional.ConditionalOnEnabledApplicationMode;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnEnabledApplicationMode(
    mode = ApplicationConfiguration.ApplicationMode.PROCESSOR_INFLUX)
public class InfluxDBService {
  private static final Logger logger = LoggerFactory.getLogger(InfluxDBService.class);

  private final InfluxDB influxDB;

  @Autowired
  public InfluxDBService(InfluxDB influxDB) {
    this.influxDB = influxDB;
  }

  public void insert(BatchPoints batchPoints) {
    this.influxDB.write(batchPoints);
  }

  public void createUser(String user, String password) throws InfluxDBServiceException {
    final var escapedUser = user.replace(" ", "");
    final var escapedPassword = password.replace("'", "\\'");

    final var queryString =
        String.format("CREATE USER \"%s\" WITH PASSWORD '%s'", escapedUser, escapedPassword);

    final var query = new Query(queryString, "_internal", true);

    logger.info("Executing query: {}", queryString);

    final var queryResult = this.influxDB.query(query);

    if (queryResult == null) {
      logger.error("Unable to create user at InfluxDB. QueryResult is null.");
      throw new InfluxDBServiceException("Unable to create user at InfluxDB. QueryResult is null");
    }

    if (queryResult.hasError()) {
      logger.error("Unable to create user at InfluxDB: {}", queryResult.getError());
      throw new InfluxDBServiceException(
          String.format("Unable to create user at InfluxDB: %s", queryResult.getError()));
    }

    logger.info("Successfully created user '{}' at InfluxDB.", escapedUser);
  }

  public void createDatabase(String name, String readOnlyUserName) throws InfluxDBServiceException {
    this.createDatabaseIfNotExistent(name);

    logger.info("Successfully created database '{}' at InfluxDB.", name);

    this.grantReadPermissionsOnDatabaseToUser(name, readOnlyUserName);
  }

  private List<String> showDatabases() throws InfluxDBServiceException {
    final var query = new Query("SHOW DATABASES", "_internal", false);
    final var queryResult = this.influxDB.query(query);

    if (queryResult == null) {
      logger.error("Unable to list InfluxDB databases. QueryResult is null.");
      throw new InfluxDBServiceException("Unable to list InfluxDB databases. QueryResult is null");
    }

    if (queryResult.hasError()) {
      logger.error("Unable to list InfluxDB databases: {}", queryResult.getError());
      throw new InfluxDBServiceException(
          String.format("Unable to list InfluxDB databases: %s", queryResult.getError()));
    }

    final var databaseNames = queryResult.getResults().get(0).getSeries().get(0).getValues();

    final var databases = new ArrayList<String>();

    if (databaseNames != null) {
      for (List<Object> database : databaseNames) {
        databases.add(database.get(0).toString());
      }
    }

    return databases;
  }

  private void createDatabaseIfNotExistent(String name) throws InfluxDBServiceException {
    if (!this.showDatabases().contains(name)) {
      logger.info(
          "There's no database for device with ID '{}' existent yet. Creating a new one.", name);
      this.createDatabase(name);
    }
  }

  private void createDatabase(String name) throws InfluxDBServiceException {
    if (StringUtils.isEmpty(name)) {
      logger.error("name == empty");
      throw new InfluxDBServiceException("name must not be empty");
    }

    final var createDatabaseQueryString = String.format("CREATE DATABASE \"%s\"", name);

    final var query = new Query(createDatabaseQueryString, "_internal", true);

    final var queryResult = this.influxDB.query(query);

    if (queryResult == null) {
      logger.error("Unable to create InfluxDB database '{}'. QueryResult is null.", name);
      throw new InfluxDBServiceException(
          String.format("Unable to create InfluxDB database '%s'. QueryResult is null", name));
    }

    if (queryResult.hasError()) {
      logger.error("Unable to create InfluxDB database: {}", queryResult.getError());
      throw new InfluxDBServiceException(
          String.format("Unable to create InfluxDB database: %s", queryResult.getError()));
    }
  }

  private void grantReadPermissionsOnDatabaseToUser(String databaseName, String userName)
      throws InfluxDBServiceException {
    final var escapedUser = userName.replace(" ", "");
    final var queryString =
        String.format("GRANT READ ON \"%s\" TO \"%s\"", databaseName, escapedUser);
    final var query = new Query(queryString, "_internal", true);

    logger.info("Executing query: {}", queryString);

    final var queryResult = this.influxDB.query(query);

    if (queryResult == null) {
      logger.error(
          "Unable to grant user '{}' read access to InfluxDB '{}'. QueryResult is null.",
          escapedUser,
          databaseName);
      throw new InfluxDBServiceException(
          "Unable to grant user read access to DB. QueryResult is null");
    }

    if (queryResult.hasError()) {
      logger.error(
          "Unable to grant user '{}' read access to InfluxDB '{}': {}",
          escapedUser,
          databaseName,
          queryResult.getError());
      throw new InfluxDBServiceException(
          String.format("Unable to grant user read access to DB: %s", queryResult.getError()));
    }

    logger.info(
        "Successfully granted user '{}' READ permissions to '{}' at InfluxDB.",
        escapedUser,
        databaseName);
  }
}
