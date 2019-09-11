package no.nav.foreldrepenger.melding.datamapper.brev;

import static no.nav.foreldrepenger.melding.datamapper.DatamapperTestUtil.BEHANDLINGSFRIST;
import static no.nav.foreldrepenger.melding.datamapper.DatamapperTestUtil.SOEKERS_NAVN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.UUID;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.xml.sax.SAXException;

import no.nav.foreldrepenger.melding.behandling.Behandling;
import no.nav.foreldrepenger.melding.behandling.BehandlingType;
import no.nav.foreldrepenger.melding.datamapper.DatamapperTestUtil;
import no.nav.foreldrepenger.melding.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.melding.dokumentdata.DokumentFelles;
import no.nav.foreldrepenger.melding.dokumentdata.DokumentMalType;
import no.nav.foreldrepenger.melding.dokumentdata.repository.DokumentRepository;
import no.nav.foreldrepenger.melding.dokumentdata.repository.DokumentRepositoryImpl;
import no.nav.foreldrepenger.melding.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.melding.hendelser.DokumentHendelse;
import no.nav.foreldrepenger.melding.integrasjon.dokument.felles.FellesType;
import no.nav.foreldrepenger.melding.integrasjon.dokument.forlenget.PersonstatusKode;
import no.nav.foreldrepenger.melding.integrasjon.dokument.forlenget.VariantKode;

public class ForlengetSaksbehandlingstidBrevMapperTest {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    private DokumentRepository dokumentRepository = new DokumentRepositoryImpl(repoRule.getEntityManager());
    private ForlengetSaksbehandlingstidBrevMapper forlengetSaksbehandlingstidBrevMapper = new ForlengetSaksbehandlingstidBrevMapper();
    private DokumentFelles dokumentFelles = DatamapperTestUtil.getDokumentFelles();
    private FellesType fellesType = DatamapperTestUtil.getFellesType();

    @Test
    public void skal_mappe_forlengelse_brev_for_FP_med_FORLENGET_OPPTJENING() throws JAXBException, SAXException, XMLStreamException {
        // Arrange
        Behandling behandling = DatamapperTestUtil.standardBehandling();
        DokumentHendelse dokumentHendelse = byggHendelse(DokumentMalType.FORLENGET_OPPTJENING, FagsakYtelseType.FORELDREPENGER);
        // Act
        String xml = forlengetSaksbehandlingstidBrevMapper.mapTilBrevXML(fellesType, dokumentFelles, dokumentHendelse, behandling);
        // Assert
        assertThat(xml).containsOnlyOnce(String.format("<ytelseType>%s</ytelseType>", FagsakYtelseType.FORELDREPENGER.getKode()));
        assertThat(xml).containsOnlyOnce(String.format("<variant>%s</variant>", VariantKode.OPPTJENING.value()));
        assertThat(xml).containsOnlyOnce(String.format("<personstatus>%s</personstatus>", PersonstatusKode.ANNET.value()));
        assertThat(xml).containsOnlyOnce(String.format("<behandlingsfristUker>%s</behandlingsfristUker>", BEHANDLINGSFRIST));
        assertThat(xml).containsOnlyOnce(String.format("<sokersNavn>%s</sokersNavn>", SOEKERS_NAVN));
    }

    @Test
    public void skal_mappe_forlengelse_brev_for_ES_med_FORLENGET_DOK() throws JAXBException, SAXException, XMLStreamException {
        // Arrange
        Behandling behandling = DatamapperTestUtil.standardBehandling();
        DokumentHendelse dokumentHendelse = byggHendelse(DokumentMalType.FORLENGET_DOK, FagsakYtelseType.ENGANGSTØNAD);
        // Act
        String xml = forlengetSaksbehandlingstidBrevMapper.mapTilBrevXML(fellesType, dokumentFelles, dokumentHendelse, behandling);
        // Assert
        assertThat(xml).containsOnlyOnce(String.format("<ytelseType>%s</ytelseType>", FagsakYtelseType.ENGANGSTØNAD.getKode()));
        assertThat(xml).containsOnlyOnce(String.format("<variant>%s</variant>", VariantKode.FORLENGET.value()));
        assertThat(xml).containsOnlyOnce(String.format("<personstatus>%s</personstatus>", PersonstatusKode.ANNET.value()));
        assertThat(xml).containsOnlyOnce(String.format("<behandlingsfristUker>%s</behandlingsfristUker>", BEHANDLINGSFRIST));
        assertThat(xml).containsOnlyOnce(String.format("<sokersNavn>%s</sokersNavn>", SOEKERS_NAVN));
    }

    @Test
    public void skal_mappe_forlengelse_brev_for_SVP_med_FORLENGET_TIDLIG_SOK() throws JAXBException, SAXException, XMLStreamException {
        // Arrange
        Behandling behandling = DatamapperTestUtil.standardBehandling();
        DokumentHendelse dokumentHendelse = byggHendelse(DokumentMalType.FORLENGET_TIDLIG_SOK, FagsakYtelseType.SVANGERSKAPSPENGER);
        // Act
        String xml = forlengetSaksbehandlingstidBrevMapper.mapTilBrevXML(fellesType, dokumentFelles, dokumentHendelse, behandling);
        // Assert
        assertThat(xml).containsOnlyOnce(String.format("<ytelseType>%s</ytelseType>", FagsakYtelseType.SVANGERSKAPSPENGER.getKode()));
        assertThat(xml).containsOnlyOnce(String.format("<variant>%s</variant>", VariantKode.FORTIDLIG.value()));
        assertThat(xml).containsOnlyOnce(String.format("<personstatus>%s</personstatus>", PersonstatusKode.ANNET.value()));
        assertThat(xml).containsOnlyOnce(String.format("<behandlingsfristUker>%s</behandlingsfristUker>", BEHANDLINGSFRIST));
        assertThat(xml).containsOnlyOnce(String.format("<sokersNavn>%s</sokersNavn>", SOEKERS_NAVN));
    }

    @Test
    public void skal_mappe_forlengelse_brev_for_SVP_med_FORLENGET_MEDL_DOK() throws JAXBException, SAXException, XMLStreamException {
        // Arrange
        Behandling behandling = DatamapperTestUtil.standardBehandling();
        DokumentHendelse dokumentHendelse = byggHendelse(DokumentMalType.FORLENGET_MEDL_DOK, FagsakYtelseType.SVANGERSKAPSPENGER);
        // Act
        String xml = forlengetSaksbehandlingstidBrevMapper.mapTilBrevXML(fellesType, dokumentFelles, dokumentHendelse, behandling);
        // Assert
        assertThat(xml).containsOnlyOnce(String.format("<ytelseType>%s</ytelseType>", FagsakYtelseType.SVANGERSKAPSPENGER.getKode()));
        assertThat(xml).containsOnlyOnce(String.format("<variant>%s</variant>", VariantKode.MEDLEM.value()));
        assertThat(xml).containsOnlyOnce(String.format("<personstatus>%s</personstatus>", PersonstatusKode.ANNET.value()));
        assertThat(xml).containsOnlyOnce(String.format("<behandlingsfristUker>%s</behandlingsfristUker>", BEHANDLINGSFRIST));
        assertThat(xml).containsOnlyOnce(String.format("<sokersNavn>%s</sokersNavn>", SOEKERS_NAVN));
    }

    @Test
    public void skal_mappe_forlengelse_brev_for_SVP_med_klagebehandling() throws JAXBException, SAXException, XMLStreamException {
        // Arrange
        BehandlingType behandlingTypeMock = Mockito.mock(BehandlingType.class);
        when(behandlingTypeMock.getBehandlingstidFristUker()).thenReturn(BEHANDLINGSFRIST);
        when(behandlingTypeMock.getKode()).thenReturn(BehandlingType.KLAGE.getKode());
        when(behandlingTypeMock.getKodeverk()).thenReturn(BehandlingType.KLAGE.getKodeverk());
        Behandling behandling = Behandling.builder().medBehandlingType(behandlingTypeMock).build();
        DokumentHendelse dokumentHendelse = byggHendelse(DokumentMalType.VEDTAK_MEDHOLD, FagsakYtelseType.SVANGERSKAPSPENGER);
        // Act
        String xml = forlengetSaksbehandlingstidBrevMapper.mapTilBrevXML(fellesType, dokumentFelles, dokumentHendelse, behandling);
        // Assert
        assertThat(xml).containsOnlyOnce(String.format("<ytelseType>%s</ytelseType>", FagsakYtelseType.SVANGERSKAPSPENGER.getKode()));
        assertThat(xml).containsOnlyOnce(String.format("<variant>%s</variant>", VariantKode.KLAGE.value()));
        assertThat(xml).containsOnlyOnce(String.format("<personstatus>%s</personstatus>", PersonstatusKode.ANNET.value()));
        assertThat(xml).containsOnlyOnce(String.format("<behandlingsfristUker>%s</behandlingsfristUker>", BEHANDLINGSFRIST));
        assertThat(xml).containsOnlyOnce(String.format("<sokersNavn>%s</sokersNavn>", SOEKERS_NAVN));
    }

    private DokumentHendelse byggHendelse(String mal, FagsakYtelseType ytelseType) {
        return DokumentHendelse.builder()
                .medBehandlingUuid(UUID.randomUUID())
                .medBestillingUuid(UUID.randomUUID())
                .medYtelseType(ytelseType)
                .medDokumentMalType(dokumentRepository.hentDokumentMalType(mal))
                .build();
    }

}