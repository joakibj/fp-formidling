package no.nav.foreldrepenger.fpformidling.integrasjon.dokgen.dto.felles;

import static no.nav.foreldrepenger.fpformidling.integrasjon.dokgen.dto.felles.FritekstDto.fra;
import static no.nav.foreldrepenger.fpformidling.integrasjon.dokgen.dto.felles.FritekstDto.ivaretaLinjeskiftIFritekst;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.fpformidling.behandling.Behandling;
import no.nav.foreldrepenger.fpformidling.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.fpformidling.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.fpformidling.hendelser.DokumentHendelse;

class FritekstTest {

    private static final String HENDELSE_FRITEKST = "HENDELSE_FRITEKST";
    private static final String BEHANDLING_FRITEKST = "BEHANDLING_FRITEKST";
    private static final UUID BEHANDLING_ID = UUID.randomUUID();
    private Behandling behandling;
    private DokumentHendelse dokumentHendelse;

    @Test
    void skal_velge_ingen_fritekst_når_ingen_finnes() {
        // Arrange
        behandling = standardBehandlingBuilder().build();
        dokumentHendelse = standardHendelseBuilder().build();

        // Act + Assert
        assertThat(fra(dokumentHendelse, behandling)).isNotPresent();
    }

    @Test
    void skal_prioritere_hendelse() {
        // Arrange
        behandling = standardBehandlingBuilder().medBehandlingsresultat(
            Behandlingsresultat.builder().medAvslagarsakFritekst(BEHANDLING_FRITEKST).build()).build();
        dokumentHendelse = standardHendelseBuilder().medFritekst(HENDELSE_FRITEKST).build();

        // Act + Assert
        assertThat(fra(dokumentHendelse, behandling).get().getFritekst()).isEqualTo(HENDELSE_FRITEKST);
    }

    @Test
    void skal_ta_fritekst_fra_behandling_når_mangler_i_hendelse() {
        // Arrange
        behandling = standardBehandlingBuilder().medBehandlingsresultat(
            Behandlingsresultat.builder().medAvslagarsakFritekst(BEHANDLING_FRITEKST).build()).build();
        var dokumentHendelse = standardHendelseBuilder().build();

        // Act + Assert
        assertThat(fra(dokumentHendelse, behandling).get().getFritekst()).isEqualTo(BEHANDLING_FRITEKST);
    }

    @Test
    void skal_bytte_ut_dokprod_formatering_med_dokgen_i_fritekst() {
        // Arrange
        behandling = standardBehandlingBuilder().medBehandlingsresultat(Behandlingsresultat.builder()
            .medAvslagarsakFritekst("Tekst\n_Overskrift\nMer tekst\n- Punkt 1\n- Punkt 2\n_Ny overskrift\nTekst-med-bindestrek_og_underscore")
            .build()).build();
        var dokumentHendelse = standardHendelseBuilder().build();

        // Act + Assert
        assertThat(fra(dokumentHendelse, behandling).get().getFritekst()).isEqualTo(
            "Tekst\n##### Overskrift\nMer tekst\n- Punkt 1\n- Punkt 2\n##### Ny overskrift\nTekst-med-bindestrek_og_underscore");
    }

    @Test
    void skal_lage_linker_av_nav_no() {
        // Arrange
        behandling = standardBehandlingBuilder().medBehandlingsresultat(Behandlingsresultat.builder()
            .medAvslagarsakFritekst("Les mer om dette på nav.no/foreldrepenger.\nDu finner mer informasjon på nav.no/klage og nav.no/familie.")
            .build()).build();
        var dokumentHendelse = standardHendelseBuilder().build();

        // Act + Assert
        assertThat(fra(dokumentHendelse, behandling).get().getFritekst()).isEqualTo(
            "Les mer om dette på [nav.no/foreldrepenger](https://nav.no/foreldrepenger).\\\nDu finner mer informasjon på [nav.no/klage](https://nav.no/klage) og [nav.no/familie](https://nav.no/familie).");
    }

    @Test
    void skal_sette_inn_ekstra_linjeskift_i_fritekst_der_det_ikke_er_punktliste() {
        // Arrange
        var fritekstInn = "Tekst 1\n- Vedlegg 1\n- Vedlegg 2\nTekst 2.\nTekst 3\n- Vedlegg 3\nTekst 4";
        var fritekstUt = "Tekst 1\n- Vedlegg 1\n- Vedlegg 2\n\nTekst 2.\\\nTekst 3\n- Vedlegg 3\n\nTekst 4";

        // Act + Assert
        assertThat(ivaretaLinjeskiftIFritekst(fritekstInn)).isEqualTo(fritekstUt);
    }

    @Test
    void skal_ikke_sette_inn_ekstra_linjeskift_når_det_bare_er_en_linje_uten_punktliste() {
        // Arrange
        var fritekstInn = "Tekst 1.";
        var fritekstUt = "Tekst 1.";

        // Act + Assert
        assertThat(ivaretaLinjeskiftIFritekst(fritekstInn)).isEqualTo(fritekstUt);
    }

    @Test
    void skal_erstatte_vanlig_linjeskift_med_slash_linjeskift() {
        // Arrange
        var fritekstInn = "Dette er en setning\nmed et linjeskift midt i.\nNy setning.";
        var fritekstUt = "Dette er en setning\\\nmed et linjeskift midt i.\\\nNy setning.";

        // Act + Assert
        assertThat(ivaretaLinjeskiftIFritekst(fritekstInn)).isEqualTo(fritekstUt);
    }

    @Test
    void skal_beholde_dobbelt_linjeskift_så_det_blir_ekstra_luft() {
        // Arrange
        var fritekstInn = "Dette er en setning.\n\nNy setning som skal ha 'luft'.";
        var fritekstUt = "Dette er en setning.\n\n\nNy setning som skal ha 'luft'.";

        // Act + Assert
        assertThat(ivaretaLinjeskiftIFritekst(fritekstInn)).isEqualTo(fritekstUt);
    }

    @Test
    void skal_ikke_sette_inn_ekstra_linjeskift_når_det_bare_er_en_linje_med_punktliste() {
        // Arrange
        var fritekstInn = "- Vedlegg1";
        var fritekstUt = "- Vedlegg1";

        // Act + Assert
        assertThat(ivaretaLinjeskiftIFritekst(fritekstInn)).isEqualTo(fritekstUt);
    }

    @Test
    void skal_gi_null_hvis_fritekst_er_null() {
        assertThat(fra(null)).isNull();
    }

    private DokumentHendelse.Builder standardHendelseBuilder() {
        return DokumentHendelse.builder()
            .medBehandlingUuid(UUID.randomUUID())
            .medBestillingUuid(UUID.randomUUID())
            .medYtelseType(FagsakYtelseType.FORELDREPENGER);
    }

    private Behandling.Builder standardBehandlingBuilder() {
        return Behandling.builder().medUuid(BEHANDLING_ID);
    }
}
