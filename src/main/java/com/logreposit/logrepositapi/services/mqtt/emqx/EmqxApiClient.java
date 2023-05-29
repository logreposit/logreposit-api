package com.logreposit.logrepositapi.services.mqtt.emqx;

import com.logreposit.logrepositapi.configuration.MqttConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmqxApiClient {
  private final MqttConfiguration mqttConfiguration;

  public EmqxApiClient(MqttConfiguration mqttConfiguration) {
    this.mqttConfiguration = mqttConfiguration;
  }

  // TODO: write implementation!

  public void dummyMethod() {
    log.info("TODO: Dummy :) MQTT Configuration: {}", mqttConfiguration);
  }

  /*
  EMQX Environment Variables:
  - "EMQX_NODE_NAME=emqx@node1.emqx.io"
  - "EMQX_LOG__CONSOLE_HANDLER__LEVEL=debug"
  - "EMQX_DASHBOARD__DEFAULT_USERNAME=administrator"
  - "EMQX_DASHBOARD__DEFAULT_PASSWORD=administrator1"
  - "EMQX_AUTHENTICATION__1__ENABLE=true"
  - "EMQX_AUTHENTICATION__1__BACKEND=built_in_database"
  - "EMQX_AUTHENTICATION__1__MECHANISM=password_based"
  - "EMQX_AUTHORIZATION__NO_MATCH=deny"
  - "EMQX_AUTHORIZATION__SOURCES__1__TYPE=file"
  - "EMQX_AUTHORIZATION__SOURCES__1__ENABLE=false"
  - "EMQX_AUTHORIZATION__SOURCES__1__PATH=etc/acl.conf"
  - "EMQX_AUTHORIZATION__SOURCES__2__ENABLE=true"
  - "EMQX_AUTHORIZATION__SOURCES__2__TYPE=built_in_database"
   */

  /*
  1. => Login =>              curl -vv -X POST http://127.0.0.1:18083/api/v5/login -H 'Content-Type: application/json' -d '{"username": "administrator","password": "administrator1"}'
  2. => List MQTT Users =>    curl -vv -X GET http://127.0.0.1:18083/api/v5/authentication/password_based:built_in_database/users -H 'Accept: application/json' -H 'Authorization: Bearer <TOKEN>'
  3. => Retrieve MQTT user =>     1. curl -vv -X GET http://127.0.0.1:18083/api/v5/authentication/password_based:built_in_database/users/myclient1 -H 'Accept: application/json' -H 'Authorization: Bearer <TOKEN>'
  4. => Create MQTT user =>   curl -vv -X POST http://127.0.0.1:18083/api/v5/authentication/password_based:built_in_database/users -H 'Content-Type: application/json' -H 'Authorization: Bearer <TOKEN>' -d '{"user_id": "myclient1", "password": "mypassword1"}'
   */
}
