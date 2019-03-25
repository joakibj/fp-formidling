package no.nav.foreldrepenger.fpsak;

import static org.terracotta.modules.ehcache.ToolkitInstanceFactoryImpl.LOGGER;

import java.net.URISyntaxException;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.http.client.utils.URIBuilder;

import no.nav.foreldrepenger.fpsak.dto.behandling.BehandlingIdDto;
import no.nav.foreldrepenger.fpsak.dto.klage.KlagebehandlingDto;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
public class KlageRestKlientImpl implements KlageRestKlient {
    //TODO burde kunne sentraliseres
    private static final String FPSAK_REST_BASE_URL = "fpsak_rest_base.url";
    private static final String HENT_KLAGE_ENDPOINT = "/fpsak/api/behandling/klage";

    private String endpointFpsakRestBase;
    private OidcRestClient oidcRestClient;

    public KlageRestKlientImpl() {
    }

    @Inject
    public KlageRestKlientImpl(OidcRestClient oidcRestClient,
                               @KonfigVerdi(FPSAK_REST_BASE_URL) String endpointFpsakRestBase) {
        this.endpointFpsakRestBase = endpointFpsakRestBase;
        this.oidcRestClient = oidcRestClient;
    }

    @Override
    public Optional<KlagebehandlingDto> hentKlagebehandling(BehandlingIdDto behandlingIdDto) {
        Optional<KlagebehandlingDto> klagebehandlingDto = Optional.empty();
        try {
            URIBuilder klageUriBuilder = new URIBuilder(endpointFpsakRestBase + HENT_KLAGE_ENDPOINT);
            klageUriBuilder.setParameter("behandlingId", String.valueOf(behandlingIdDto.getBehandlingId()));
            klagebehandlingDto = oidcRestClient.getReturnsOptional(klageUriBuilder.build(), KlagebehandlingDto.class);
        } catch (URISyntaxException e) {
            LOGGER.error("Feil ved oppretting av URI.", e);
        }
        return klagebehandlingDto;
    }
}