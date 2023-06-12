package com.mysandbox.keycloak;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.keycloak.Config.Scope;

import org.jboss.logging.Logger;

public final class KafkaStandardProducerFactory {
  private Properties propertyMap;
  private String username;
  private String password;
  private String bootstrapServers;
  public String adminTopicPrefix;
  public String topicPrefix;
  private boolean kafkaEnabled;

  private static final Logger logger = Logger.getLogger(KafkaStandardProducerFactory.class);

  public KafkaStandardProducerFactory(Scope scope) {
    this.bootstrapServers = scope.get("bootstrapServers", System.getenv("KAFKA_BOOTSTRAP_SERVERS"));
    if (bootstrapServers == null) {
      this.kafkaEnabled = false;
    } else {
      this.kafkaEnabled = true;
    }
    this.propertyMap = init(scope);
    this.username = scope.get("username", System.getenv("KAFKA_USERNAME"));
    this.password = scope.get("password", System.getenv("KAFKA_PASSWORD"));
    this.topicPrefix = scope.get("topicPrefix", System.getenv("KAFKA_TOPIC_PREFIX"));
    this.adminTopicPrefix = scope.get("adminTopicPrefix", System.getenv("KAFKA_ADMIN_TOPIC_PREFIX"));
  }

  public static Properties init(Scope scope) {
    Properties propertyMap = new Properties();
    Object[] producerProperties = ProducerConfig.configNames().toArray();

    for (Object property : producerProperties) {
      String propName = property.toString();
      if (propName != null && scope.get(propName) != null) {
        propertyMap.put(propName, scope.get(propName));
      }
    }
    return propertyMap;
  }

  public Producer<String, String> createProducer() {
    String serializer = StringSerializer.class.getName();
    if (!this.kafkaEnabled) {
      return null;
    }

    Properties props = new Properties(propertyMap);
    props = new Properties();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.bootstrapServers);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, serializer);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, serializer);
    props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 3000);
    props.put(ProducerConfig.LINGER_MS_CONFIG, 300);
    props.put("security.protocol", "SSL");

    if (username != null && password != null) {
      String jaasTemplate = "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";";
      String jaasCfg = String.format(jaasTemplate, username, password);
      logger.info("Kafka Producer authenticated by username and password");
      props.put("security.protocol", "SASL_SSL");
      props.put("sasl.mechanism", "SCRAM-SHA-256");
      props.put("sasl.jaas.config", jaasCfg);
    }

    // fix Class org.apache.kafka.common.serialization.StringSerializer could not be
    // found. see https://stackoverflow.com/a/50981469
    Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());

    return new KafkaProducer<>(props);
  }
}
