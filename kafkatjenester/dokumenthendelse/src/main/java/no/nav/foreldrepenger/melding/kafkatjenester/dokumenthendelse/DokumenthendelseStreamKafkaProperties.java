package no.nav.foreldrepenger.melding.kafkatjenester.dokumenthendelse;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.apache.kafka.common.serialization.Serdes;

import no.nav.vedtak.konfig.KonfigVerdi;

@Dependent
public class DokumenthendelseStreamKafkaProperties {

    private final String bootstrapServers;
    private final String schemaRegistryUrl;
    private final String username;
    private final String password;
    private final String topic;
    private final String applicationId;

    @Inject
    DokumenthendelseStreamKafkaProperties(@KonfigVerdi("kafka.bootstrap.servers") String bootstrapServers,
                                          @KonfigVerdi("kafka.dokumenthendelse.schema.registry.url") String schemaRegistryUrl,
                                          @KonfigVerdi("systembruker.username") String username,
                                          @KonfigVerdi("systembruker.password") String password,
                                          @KonfigVerdi("kafka.dokumenthendelse.topic") String topic) {
        this.topic = topic;
        this.applicationId = ApplicationIdUtil.get();
        this.bootstrapServers = bootstrapServers;
        this.schemaRegistryUrl = schemaRegistryUrl;
        this.username = username;
        this.password = password;
    }

    String getBootstrapServers() {
        return bootstrapServers;
    }

    String getSchemaRegistryUrl() {
        return schemaRegistryUrl;
    }

//    String getClientId() {
//        return topic.getConsumerClientId();
//    }

    String getUsername() {
        return username;
    }

    String getPassword() {
        return password;
    }

    String getTopic() {
        return topic;
    }

//    Class<?> getKeyClass() {
//        return topic.getSerdeKey().getClass();
//    }

//    Class<?> getValueClass() {
//        return topic.getSerdeValue().getClass();
//    }

    boolean harSattBrukernavn() {
        return username != null && !username.isEmpty();
    }

    String getApplicationId() {
        return applicationId;
    }

    String getClientId() {
        return "KC-" + topic;
    }

    Class<?> getValueClass() {
        return Serdes.String().getClass(); //TODO
    }

    Class<?> getKeyClass() {
        return Serdes.String().getClass();  //TODO
    }
}