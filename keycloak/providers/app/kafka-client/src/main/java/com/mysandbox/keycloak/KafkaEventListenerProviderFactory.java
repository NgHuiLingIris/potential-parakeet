package com.mysandbox.keycloak;

import java.util.Map;

import com.mysandbox.keycloak.KafkaStandardProducerFactory;

import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class KafkaEventListenerProviderFactory implements EventListenerProviderFactory {
    private KafkaEventListenerProvider INSTANCE;
    private KafkaStandardProducerFactory factory;

    @Override
    public String getId() {
        return "kafka-event-listener";
    }

    @Override
    public void init(Config.Scope config) {
        factory = new KafkaStandardProducerFactory(config);
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public EventListenerProvider create(KeycloakSession session) {
        if (INSTANCE == null) {
            INSTANCE = new KafkaEventListenerProvider(session, factory);
        }
        return INSTANCE;
    }

    @Override
    public void close() {
    }
}
