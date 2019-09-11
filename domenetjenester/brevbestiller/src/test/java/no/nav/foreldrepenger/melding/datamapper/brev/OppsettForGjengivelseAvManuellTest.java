package no.nav.foreldrepenger.melding.datamapper.brev;

import static no.nav.foreldrepenger.melding.datamapper.mal.fritekst.BrevmalKilder.ROTMAPPE;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityManager;

import org.json.JSONObject;
import org.junit.Rule;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.foreldrepenger.fpsak.BehandlingRestKlient;
import no.nav.foreldrepenger.fpsak.BehandlingRestKlientImpl;
import no.nav.foreldrepenger.fpsak.dto.behandling.BehandlingDto;
import no.nav.foreldrepenger.melding.behandling.Behandling;
import no.nav.foreldrepenger.melding.behandling.BehandlingResourceLink;
import no.nav.foreldrepenger.melding.datamapper.DomeneobjektProvider;
import no.nav.foreldrepenger.melding.datamapper.dto.AksjonspunktDtoMapper;
import no.nav.foreldrepenger.melding.datamapper.konfig.BrevParametere;
import no.nav.foreldrepenger.melding.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.melding.dokumentdata.DokumentFelles;
import no.nav.foreldrepenger.melding.dtomapper.BehandlingDtoMapper;
import no.nav.foreldrepenger.melding.dtomapper.BehandlingsresultatDtoMapper;
import no.nav.foreldrepenger.melding.dtomapper.BeregningsgrunnlagDtoMapper;
import no.nav.foreldrepenger.melding.dtomapper.BeregningsresultatDtoMapper;
import no.nav.foreldrepenger.melding.dtomapper.FagsakDtoMapper;
import no.nav.foreldrepenger.melding.dtomapper.FamiliehendelseDtoMapper;
import no.nav.foreldrepenger.melding.dtomapper.IAYDtoMapper;
import no.nav.foreldrepenger.melding.dtomapper.InnsynDtoMapper;
import no.nav.foreldrepenger.melding.dtomapper.KlageDtoMapper;
import no.nav.foreldrepenger.melding.dtomapper.MottattDokumentDtoMapper;
import no.nav.foreldrepenger.melding.dtomapper.StønadskontoDtoMapper;
import no.nav.foreldrepenger.melding.dtomapper.UttakDtoMapper;
import no.nav.foreldrepenger.melding.dtomapper.UttakSvpDtoMapper;
import no.nav.foreldrepenger.melding.dtomapper.VilkårDtoMapper;
import no.nav.foreldrepenger.melding.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.melding.hendelser.DokumentHendelse;
import no.nav.foreldrepenger.melding.integrasjon.dokument.felles.FellesType;
import no.nav.foreldrepenger.melding.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.melding.kodeverk.KodeverkRepositoryImpl;
import no.nav.foreldrepenger.tps.impl.TpsAdapterImpl;
import no.nav.foreldrepenger.tps.impl.TpsOversetter;
import no.nav.foreldrepenger.tps.impl.TpsTjenesteImpl;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.AktoerIder;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumer;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumerMedCache;
import no.nav.vedtak.felles.integrasjon.person.PersonConsumer;
import no.nav.vedtak.felles.integrasjon.rest.JsonMapper;
import no.nav.vedtak.util.StringUtils;

public abstract class OppsettForGjengivelseAvManuellTest {
    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule().silent();
    @Mock
    BrevParametere brevParametere;
    @Mock
    DokumentFelles dokumentFelles;
    @Mock
    FellesType fellesType;

    private JSONObject testdata;
    protected ResourceBundle testScenario = ResourceBundle.getBundle(
            String.join("/", ROTMAPPE, mappenHvorFilenMedLoggetTestdataLigger(), "testdata"),
            new Locale("nb", "NO"));

    protected Behandling behandling;
    protected DokumentHendelse dokumentHendelse;

    private EntityManager entityManager = repoRule.getEntityManager();

    private KodeverkRepository kodeverkRepository = new KodeverkRepositoryImpl(entityManager);
    private BehandlingRestKlient behandlingRestKlient = new RedirectedToJsonResource();
    private  BehandlingDtoMapper behandlingDtoMapper = new BehandlingDtoMapper(kodeverkRepository,
            behandlingRestKlient,
            null,
            new BehandlingsresultatDtoMapper(kodeverkRepository));

    protected DomeneobjektProvider domeneobjektProvider = new DomeneobjektProvider(behandlingRestKlient,
            new BeregningsgrunnlagDtoMapper(kodeverkRepository),
            this.behandlingDtoMapper,
            new BeregningsresultatDtoMapper(behandlingRestKlient,
                    kodeverkRepository),
            new KlageDtoMapper(kodeverkRepository),
            new UttakDtoMapper(kodeverkRepository),
            new UttakSvpDtoMapper(kodeverkRepository),
            new IAYDtoMapper(kodeverkRepository),
            new InnsynDtoMapper(kodeverkRepository),
            new VilkårDtoMapper(kodeverkRepository),
            new FamiliehendelseDtoMapper(),
            new StønadskontoDtoMapper(kodeverkRepository),
            new AksjonspunktDtoMapper(),
            new MottattDokumentDtoMapper(kodeverkRepository),
            new FagsakDtoMapper(kodeverkRepository, mockTpsTjeneste())
    );

    private TpsTjenesteImpl mockTpsTjeneste() {
        return new TpsTjenesteImpl(new TpsAdapterImpl(
                new AktørConsumerMedCache(new AktørConsumer() {
                    @Override
                    public Optional<String> hentAktørIdForPersonIdent(String s) {
                        return Optional.of("123456");
                    }

                    @Override
                    public Optional<String> hentPersonIdentForAktørId(String s) {
                        return Optional.of("654321");
                    }

                    @Override
                    public List<AktoerIder> hentAktørIdForPersonIdentSet(Set<String> set) {
                        return Collections.emptyList();
                    }
                }),
                Mockito.mock(PersonConsumer.class),
                Mockito.mock(TpsOversetter.class)));
    }

    protected void setup(String scenario) {
        testdata = new JSONObject(testScenario.getString(scenario));
        String behandlingJson = testdata.getString("null");
        BehandlingDto dto = JsonMapper.fromJson(behandlingJson, BehandlingDto.class);
        behandling = Behandling.builder(behandlingDtoMapper.mapBehandlingFraDto(dto)).build();
        dokumentHendelse = DokumentHendelse.builder()
                .medBehandlingUuid(UUID.randomUUID())
                .medBestillingUuid(UUID.randomUUID())
                .medYtelseType(FagsakYtelseType.FORELDREPENGER)
                .build();
    }

    abstract String mappenHvorFilenMedLoggetTestdataLigger();

    private class RedirectedToJsonResource extends BehandlingRestKlientImpl {

        @Override
        protected <T> Optional<T> hentDtoFraLink(BehandlingResourceLink link, Class<T> clazz) {
            String entity = testdata.getString(link.getRel());
            if (StringUtils.nullOrEmpty(entity)) {
                return Optional.empty();
            }
            return Optional.of(JsonMapper.fromJson(entity, clazz));
        }
    }
}