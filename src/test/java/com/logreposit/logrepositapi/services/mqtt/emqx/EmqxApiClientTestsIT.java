package com.logreposit.logrepositapi.services.mqtt.emqx;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logreposit.logrepositapi.configuration.MqttConfiguration;
import com.logreposit.logrepositapi.services.mqtt.emqx.dtos.AuthAction;
import com.logreposit.logrepositapi.services.mqtt.emqx.dtos.AuthPermission;
import com.logreposit.logrepositapi.services.mqtt.emqx.dtos.EmqxAuthRule;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties(value = MqttConfiguration.class)
@SpringBootTest(
    classes = {
      EmqxApiClient.class,
      MqttConfiguration.class,
      RestTemplateAutoConfiguration.class,
      ObjectMapper.class
    })
public class EmqxApiClientTestsIT {
  @Autowired private EmqxApiClient emqxApiClient;

  @Test
  public void testAsdf() throws InterruptedException {
    final var userName = "myTestApiUser5";

    final var alreadyExistingUser = this.emqxApiClient.retrieveEmqxAuthUser(userName);

    assertThat(alreadyExistingUser).isEmpty();

    final var createdUser = this.emqxApiClient.createEmqxAuthUser(userName, "mypassword1");

    assertThat(createdUser.getUserId()).isEqualTo(userName);
    assertThat(createdUser.getSuperuser()).isFalse();

    this.emqxApiClient.createRulesForAuthUser(
        userName,
        List.of(
            EmqxAuthRule.builder()
                .topic("myUser/" + userName + "/#")
                .permission(AuthPermission.ALLOW)
                .action(AuthAction.SUBSCRIBE)
                .build()));

    Thread.sleep(1_000);

    // 409 Conflict: "{"code":"ALREADY_EXISTS","message":"Users 'myTestApiUser3' already exist"}"
    // When some rules already exist for this user
    this.emqxApiClient.createRulesForAuthUser(
        userName,
        List.of(
            EmqxAuthRule.builder()
                .topic("myOtherUser/" + userName + "/#")
                .permission(AuthPermission.ALLOW)
                .action(AuthAction.SUBSCRIBE)
                .build()));

    this.emqxApiClient.deleteEmqxAuthUser(userName);
    this.emqxApiClient.deleteRulesOfAuthUser(userName);
    this.emqxApiClient.deleteRulesOfAuthUser(userName + "invalid");

    Thread.sleep(1_000);
  }
}
