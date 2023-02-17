package no.nav.foreldrepenger.fpformidling.web.app.konfig;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.server.ServerProperties;

import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.integration.GenericOpenApiContextBuilder;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import no.nav.foreldrepenger.fpformidling.web.app.exceptions.ConstraintViolationMapper;
import no.nav.foreldrepenger.fpformidling.web.app.exceptions.GeneralRestExceptionMapper;
import no.nav.foreldrepenger.fpformidling.web.app.exceptions.JsonMappingExceptionMapper;
import no.nav.foreldrepenger.fpformidling.web.app.exceptions.JsonParseExceptionMapper;
import no.nav.foreldrepenger.fpformidling.web.app.jackson.JacksonJsonConfig;
import no.nav.foreldrepenger.fpformidling.web.app.tjenester.ForvaltningRestTjeneste;
import no.nav.foreldrepenger.fpformidling.web.app.tjenester.brev.BrevRestTjeneste;
import no.nav.foreldrepenger.fpformidling.web.server.jetty.TimingFilter;
import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.prosesstask.rest.ProsessTaskRestTjeneste;

@ApplicationPath(ApiConfig.API_URI)
public class ApiConfig extends Application {

    private static final Environment ENV = Environment.current();
    static final String API_URI = "/api";

    public ApiConfig() {
        OpenAPI oas = new OpenAPI();
        Info info = new Info()
                .title("Vedtaksløsningen - Formidling")
                .version("1.0")
                .description("REST grensesnitt for fp-formidling. Til å kunne bruke tjenestene må en gyldig token være tilstede.");

        oas.info(info)
                .addServersItem(new Server()
                        .url(ENV.getProperty("context.path", "/fpformidling")));

        SwaggerConfiguration oasConfig = new SwaggerConfiguration()
                .openAPI(oas)
                .prettyPrint(true)
                .resourceClasses(getClasses().stream().map(Class::getName).collect(Collectors.toSet()));
        try {
            new GenericOpenApiContextBuilder<>()
                    .openApiConfiguration(oasConfig)
                    .buildContext(true)
                    .read();
        } catch (OpenApiConfigurationException e) {
            throw new TekniskException("OPEN-API", e.getMessage(), e);
        }
    }

    @Override
    public Set<Class<?>> getClasses() {
        // eksponert grensesnitt

        Set<Class<?>> classes = new HashSet<>(getAllClasses());

        // swagger
        classes.add(OpenApiResource.class);

        // Applikasjonsoppsett
        classes.add(TimingFilter.class);
        classes.add(JacksonJsonConfig.class);

        // ExceptionMappers pga de som finnes i Jackson+Jersey-media
        classes.add(ConstraintViolationMapper.class);
        classes.add(JsonMappingExceptionMapper.class);
        classes.add(JsonParseExceptionMapper.class);

        // Generell exceptionmapper m/logging for øvrige tilfelle
        classes.add(GeneralRestExceptionMapper.class);

        return Collections.unmodifiableSet(classes);
    }

    private static Collection<Class<?>> getAllClasses() {
        Set<Class<?>> classes = new HashSet<>();
        classes.add(BrevRestTjeneste.class);
        classes.add(ProsessTaskRestTjeneste.class);
        classes.add(ForvaltningRestTjeneste.class);
        return classes;
    }

    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> properties = new HashMap<>();
        // Ref Jersey doc
        properties.put(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true);
        properties.put(ServerProperties.PROCESSING_RESPONSE_ERRORS_ENABLED, true);
        return properties;
    }
}