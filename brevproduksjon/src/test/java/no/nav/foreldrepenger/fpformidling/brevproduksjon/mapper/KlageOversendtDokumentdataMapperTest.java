package no.nav.foreldrepenger.fpformidling.brevproduksjon.mapper;

import static no.nav.foreldrepenger.fpformidling.brevproduksjon.mapper.felles.BrevMapperUtil.formaterPersonnummer;
import static no.nav.foreldrepenger.fpformidling.brevproduksjon.mapper.felles.DatamapperTestUtil.SAKSNUMMER;
import static no.nav.foreldrepenger.fpformidling.brevproduksjon.mapper.felles.DatamapperTestUtil.SØKERS_FNR;
import static no.nav.foreldrepenger.fpformidling.brevproduksjon.mapper.felles.DatamapperTestUtil.SØKERS_NAVN;
import static no.nav.foreldrepenger.fpformidling.brevproduksjon.mapper.felles.DatamapperTestUtil.lagStandardDokumentData;
import static no.nav.foreldrepenger.fpformidling.brevproduksjon.mapper.felles.DatamapperTestUtil.lagStandardDokumentFelles;
import static no.nav.foreldrepenger.fpformidling.brevproduksjon.mapper.felles.DatamapperTestUtil.lagStandardHendelseBuilder;
import static no.nav.foreldrepenger.fpformidling.brevproduksjon.mapper.felles.DatamapperTestUtil.standardBehandling;
import static no.nav.foreldrepenger.fpformidling.typer.Dato.formaterDatoNorsk;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.foreldrepenger.fpformidling.behandling.Behandling;
import no.nav.foreldrepenger.fpformidling.behandling.BehandlingType;
import no.nav.foreldrepenger.fpformidling.brevproduksjon.tjenester.DomeneobjektProvider;
import no.nav.foreldrepenger.fpformidling.dokumentdata.DokumentData;
import no.nav.foreldrepenger.fpformidling.dokumentdata.DokumentFelles;
import no.nav.foreldrepenger.fpformidling.integrasjon.dokgen.dto.felles.FritekstDto;
import no.nav.foreldrepenger.fpformidling.klage.Klage;
import no.nav.foreldrepenger.fpformidling.klage.KlageDokument;
import no.nav.foreldrepenger.fpformidling.klage.KlageVurderingResultat;
import no.nav.foreldrepenger.fpformidling.kodeverk.kodeverdi.DokumentMalType;

@ExtendWith(MockitoExtension.class)
class KlageOversendtDokumentdataMapperTest {

    private static final String FRITEKST_TIL_BREV = "FRITEKST";
    private static final LocalDate MOTTATT_DATO = LocalDate.now();

    @Mock
    private DomeneobjektProvider domeneobjektProvider;

    private DokumentData dokumentData;

    private KlageOversendtDokumentdataMapper dokumentdataMapper;

    @BeforeEach
    void before() {
        dokumentData = lagStandardDokumentData(DokumentMalType.KLAGE_OVERSENDT);
        when(domeneobjektProvider.hentKlageDokument(any(Behandling.class))).thenReturn(new KlageDokument(MOTTATT_DATO));
        dokumentdataMapper = new KlageOversendtDokumentdataMapper(domeneobjektProvider);
    }

    @Test
    void skal_mappe_felter_for_brevet() {
        // Arrange
        var behandling = standardBehandling();
        var dokumentFelles = lagStandardDokumentFelles(dokumentData, DokumentFelles.Kopi.JA, false);
        var dokumentHendelse = lagStandardHendelseBuilder().medErOpphevetKlage(true).build();
        mockKlage(behandling);

        // Act
        var dokumentdata = dokumentdataMapper.mapTilDokumentdata(dokumentFelles, dokumentHendelse, behandling, false);

        // Assert
        assertThat(dokumentdata.getFelles()).isNotNull();
        assertThat(dokumentdata.getFelles().getSøkerNavn()).isEqualTo(SØKERS_NAVN);
        assertThat(dokumentdata.getFelles().getSøkerPersonnummer()).isEqualTo(formaterPersonnummer(SØKERS_FNR));
        assertThat(dokumentdata.getFelles().getMottakerNavn()).isNull();
        assertThat(dokumentdata.getFelles().getBrevDato()).isEqualTo(formaterDatoNorsk(LocalDate.now()));
        assertThat(dokumentdata.getFelles().getHarVerge()).isTrue();
        assertThat(dokumentdata.getFelles().getErKopi()).isTrue();
        assertThat(dokumentdata.getFelles().getSaksnummer()).isEqualTo(SAKSNUMMER);
        assertThat(dokumentdata.getFelles().getYtelseType()).isEqualTo("FP");
        assertThat(dokumentdata.getFelles().getBehandlesAvKA()).isFalse();
        assertThat(dokumentdata.getFelles().getErUtkast()).isFalse();
        assertThat(dokumentdata.getFelles().getFritekst()).isEqualTo(FritekstDto.fra(FRITEKST_TIL_BREV));

        assertThat(dokumentdata.getGjelderTilbakekreving()).isFalse();
        assertThat(dokumentdata.getMottattDato()).isEqualTo(formaterDatoNorsk(MOTTATT_DATO));
    }

    private void mockKlage(Behandling behandling) {
        var klage = Klage.ny()
            .medPåklagdBehandlingType(BehandlingType.FØRSTEGANGSSØKNAD)
            .medKlageVurderingResultatNK(new KlageVurderingResultat(null, FRITEKST_TIL_BREV))
            .build();
        when(domeneobjektProvider.hentKlagebehandling(behandling)).thenReturn(klage);
    }
}
