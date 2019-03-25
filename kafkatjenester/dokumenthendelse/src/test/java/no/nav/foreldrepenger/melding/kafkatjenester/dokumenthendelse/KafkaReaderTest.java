package no.nav.foreldrepenger.melding.kafkatjenester.dokumenthendelse;

import static org.assertj.core.api.Assertions.assertThat;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import com.fasterxml.jackson.core.JsonProcessingException;

import no.nav.foreldrepenger.melding.behandling.BehandlingType;
import no.nav.foreldrepenger.melding.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.melding.dokumentdata.DokumentMalType;
import no.nav.foreldrepenger.melding.dokumentdata.repository.DokumentRepository;
import no.nav.foreldrepenger.melding.dokumentdata.repository.DokumentRepositoryImpl;
import no.nav.foreldrepenger.melding.dtomapper.DtoTilDomeneobjektMapper;
import no.nav.foreldrepenger.melding.eventmottak.EventmottakFeillogg;
import no.nav.foreldrepenger.melding.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.melding.hendelsekontrakter.hendelse.DokumentHendelseDto;
import no.nav.foreldrepenger.melding.hendelser.HendelseRepository;
import no.nav.foreldrepenger.melding.hendelser.HendelseRepositoryImpl;
import no.nav.foreldrepenger.melding.historikk.HistorikkAktør;
import no.nav.foreldrepenger.melding.kafkatjenester.felles.util.Serialiseringsverktøy;
import no.nav.foreldrepenger.melding.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.melding.kodeverk.KodeverkRepositoryImpl;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

public class KafkaReaderTest {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule().silent();
    private EntityManager entityManager = repoRule.getEntityManager();
    private HendelseRepository hendelseRepository;
    @Mock
    private ProsessTaskRepository prosessTaskRepository;
    private KodeverkRepository kodeverkRepository;
    private DokumentRepository dokumentRepository;
    private JsonHendelseHandler jsonHendelseHandler;
    private DtoTilDomeneobjektMapper dtoTilDomeneobjektMapper;


    private KafkaReader kafkaReader;
    private long behandlingId = 123l;

    @Before
    public void setup() {
        hendelseRepository = new HendelseRepositoryImpl(entityManager);
        kodeverkRepository = new KodeverkRepositoryImpl(entityManager);
        dokumentRepository = new DokumentRepositoryImpl(entityManager);
        dtoTilDomeneobjektMapper = new DtoTilDomeneobjektMapper(kodeverkRepository, dokumentRepository);
        jsonHendelseHandler = new JsonHendelseHandler(hendelseRepository, prosessTaskRepository, dtoTilDomeneobjektMapper);
        this.kafkaReader = new KafkaReader(null, jsonHendelseHandler, hendelseRepository);
    }

    @Test
    public void normal_melding_skal_prossesseres_ok() {
        String json = serialiser(lagOkDto());
        kafkaReader.prosesser(json);
        assertThat(repoRule.getRepository().hentAlle(EventmottakFeillogg.class)).hasSize(0);
    }

    @Test
    public void ugyldig_melding_skal_logges() {
        kafkaReader.prosesser("Bare tull");
        assertThat(repoRule.getRepository().hentAlle(EventmottakFeillogg.class)).hasSize(1);
        assertThat(repoRule.getRepository().hentAlle(EventmottakFeillogg.class).get(0).getMelding()).isEqualToIgnoringCase("Bare tull");
    }

    private String serialiser(DokumentHendelseDto dto) {
        try {
            return Serialiseringsverktøy.getObjectMapper().writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private DokumentHendelseDto lagOkDto() {
        DokumentHendelseDto dto = new DokumentHendelseDto();
        dto.setBehandlingId(behandlingId);
        dto.setBehandlingType(BehandlingType.FØRSTEGANGSSØKNAD.getKode());
        dto.setDokumentMal(DokumentMalType.AVSLAGSVEDTAK_DOK);
        dto.setFritekst("123");
        dto.setTittel("tittel");
        dto.setGjelderVedtak(false);
        dto.setHistorikkAktør(HistorikkAktør.BESLUTTER.getKode());
        dto.setYtelseType(FagsakYtelseType.FORELDREPENGER.getKode());
        return dto;
    }

}