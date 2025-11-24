package com.logreposit.logrepositapi.services.influxdb;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
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
    String escapedUser = user.replace(" ", "");
    String escapedPassword = password.replace("'", "\\'");
    String queryString =
        String.format("CREATE USER \"%s\" WITH PASSWORD '%s'", escapedUser, escapedPassword);
    Query query = new Query(queryString, "_internal", true);

    logger.info("Executing query: {}", queryString);

    QueryResult queryResult = this.influxDB.query(query);

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
    Query query = new Query("SHOW DATABASES", "_internal", false);
    QueryResult queryResult = this.influxDB.query(query);

    if (queryResult == null) {
      logger.error("Unable to list InfluxDB databases. QueryResult is null.");
      throw new InfluxDBServiceException("Unable to list InfluxDB databases. QueryResult is null");
    }

    if (queryResult.hasError()) {
      logger.error("Unable to list InfluxDB databases: {}", queryResult.getError());
      throw new InfluxDBServiceException(
          String.format("Unable to list InfluxDB databases: %s", queryResult.getError()));
    }

    List<List<Object>> databaseNames =
        queryResult.getResults().get(0).getSeries().get(0).getValues();

    List<String> databases = new ArrayList<>();

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

    String createDatabaseQueryString = String.format("CREATE DATABASE \"%s\"", name);

    Query query = new Query(createDatabaseQueryString, "_internal", true);

    QueryResult queryResult = this.influxDB.query(query);

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
    String escapedUser = userName.replace(" ", "");
    String queryString = String.format("GRANT READ ON \"%s\" TO \"%s\"", databaseName, escapedUser);
    Query query = new Query(queryString, "_internal", true);

    logger.info("Executing query: {}", queryString);

    QueryResult queryResult = this.influxDB.query(query);

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
