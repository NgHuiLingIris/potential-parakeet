package com.mysandbox.keycloak;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.jboss.logging.Logger;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class KafkaEventListenerProvider implements EventListenerProvider {

  private static final Logger logger = Logger.getLogger(KafkaEventListenerProvider.class);
  private Producer<String, String> producer;
  private KafkaStandardProducerFactory factory;
  private KeycloakSession session;

  public KafkaEventListenerProvider(KeycloakSession session, KafkaStandardProducerFactory factory) {
    logger.info("Initializing KafkaEventListenerProvider");
    this.factory = factory;
    this.session = session;
    producer = factory.createProducer();
    logger.info("Complete initializing KafkaProducer " + producer.toString());
  }

  private String getTopic(Event event, AdminEvent adminEvent) {
    String realm = "";
    String prefix = "";
    realm = "realm"; // TO DO: get realm name instead
    prefix = factory.topicPrefix != null ? factory.topicPrefix + "-" : prefix;
    String topic = prefix + realm;
    return topic;
  }

  @Override
  public void onEvent(Event event) {
    if (event == null || event.getUserId() == null) {
      return;
    }
    String kafkaMsg = generateMsg(event);
    try {
      String topic = getTopic(event, null);
      produceEvent(kafkaMsg, topic);
    } catch (Exception e) {
      logger.errorf(e, "Failed to send kafka notification, %s", kafkaMsg.toString());
    }
  }

  private void produceEvent(String event, String topic)
      throws InterruptedException, ExecutionException, TimeoutException {
    ProducerRecord<String, String> record = new ProducerRecord<>(topic, event.toString());
    producer.send(record, new Callback() {
      public void onCompletion(RecordMetadata metadata, Exception exception) {
              if (exception != null) {
                  logger.error("Error while producing message to topic :" + metadata, exception);
              } else {
                  logger.info("sent message to topic:" + metadata.topic() + " partition:" + metadata.partition() + " offset:" + metadata.offset() + " timestamp:" + metadata.timestamp());
              }
          }
      });
  }

  @Override
  public void close() {

  }

  @Override
  public void onEvent(AdminEvent event, boolean includeRepresentation) {
    // TODO: Add adminevent generator
  }

  private String generateMsg(Event event) {
    ObjectMapper mapper = new ObjectMapper();
    try {
      String resp = mapper.writeValueAsString(event);
      return resp;
    } catch (JsonProcessingException e) {
      logger.errorf(e, "Unable to generate JSON Kafka message");
      return null;
    }
  }
}
