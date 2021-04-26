package no.nav.foreldrepenger.melding.brevmapper.brev.innvilgelsefp;

import static java.util.List.of;
import static no.nav.foreldrepenger.melding.brevmapper.brev.innvilgelsefp.BeregningsgrunnlagMapper.finnBrutto;
import static no.nav.foreldrepenger.melding.brevmapper.brev.innvilgelsefp.BeregningsgrunnlagMapper.finnSeksG;
import static no.nav.foreldrepenger.melding.brevmapper.brev.innvilgelsefp.BeregningsgrunnlagMapper.harBruktBruttoBeregningsgrunnlag;
import static no.nav.foreldrepenger.melding.brevmapper.brev.innvilgelsefp.BeregningsgrunnlagMapper.inntektOverSeksG;
import static no.nav.foreldrepenger.melding.brevmapper.brev.innvilgelsefp.BeregningsgrunnlagMapper.mapRegelListe;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.melding.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.melding.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.melding.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatus;
import no.nav.foreldrepenger.melding.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.melding.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.melding.integrasjon.dokgen.dto.innvilgelsefp.BeregningsgrunnlagRegel;
import no.nav.foreldrepenger.melding.typer.Beløp;

public class BeregningsgrunnlagMapperTest {

    private static final BigDecimal AVKORTET_PR_ÅR = BigDecimal.valueOf(60);
    private static final BigDecimal FRILANSER_BRUTTO_PR_ÅR = BigDecimal.valueOf(50000);
    private static final BigDecimal ARBEIDSTAKER_BRUTTO_PR_ÅR = BigDecimal.valueOf(100000);
    private static final BigDecimal GRUNNBELØP = BigDecimal.valueOf(50_000);
    private static final long STANDARD_PERIODE_DAGSATS = 100L;
    private static final long FRILANSER_DAGSATS = 200L;
    private static final long ARBEIDSTAKER_DAGSATS = 300L;

    @Test
    public void skal_finne_brutto() {
        // Arrange
        Beregningsgrunnlag beregningsgrunnlag = Beregningsgrunnlag.ny()
                .leggTilBeregningsgrunnlagPeriode(lagBeregningsgrunnlagPeriode())
                .build();

        // Act + Assert
        assertThat(finnBrutto(beregningsgrunnlag)).isEqualTo(FRILANSER_BRUTTO_PR_ÅR.add(AVKORTET_PR_ÅR).longValue());
    }

    @Test
    public void skal_finne_seksG() {
        // Arrange
        Beregningsgrunnlag beregningsgrunnlag = Beregningsgrunnlag.ny()
                .medGrunnbeløp(new Beløp(GRUNNBELØP))
                .leggTilBeregningsgrunnlagPeriode(lagBeregningsgrunnlagPeriode())
                .build();

        // Act + Assert
        assertThat(finnSeksG(beregningsgrunnlag)).isEqualTo(GRUNNBELØP.multiply(BigDecimal.valueOf(6)));
    }

    @Test
    public void skal_identifisere_brutto_over_6g() {
        // Arrange
        Beregningsgrunnlag beregningsgrunnlag = Beregningsgrunnlag.ny()
                .medGrunnbeløp(new Beløp(GRUNNBELØP))
                .leggTilBeregningsgrunnlagPeriode(
                        BeregningsgrunnlagPeriode.ny()
                                .medBruttoPrÅr(GRUNNBELØP.multiply(BigDecimal.valueOf(6)).add(BigDecimal.ONE))
                                .build())
                .build();

        // Act + Assert
        assertThat(inntektOverSeksG(beregningsgrunnlag)).isTrue();
    }

    @Test
    public void skal_identifisere_ikke_brutto_over_6g() {
        // Arrange
        Beregningsgrunnlag beregningsgrunnlag = Beregningsgrunnlag.ny()
                .medGrunnbeløp(new Beløp(GRUNNBELØP))
                .leggTilBeregningsgrunnlagPeriode(
                        BeregningsgrunnlagPeriode.ny()
                                .medBruttoPrÅr(GRUNNBELØP.multiply(BigDecimal.valueOf(6)))
                                .build())
                .build();

        // Act + Assert
        assertThat(inntektOverSeksG(beregningsgrunnlag)).isFalse();
    }

    @Test
    public void skal_mappe_regelListe() {
        // Arrange
        Beregningsgrunnlag beregningsgrunnlag = Beregningsgrunnlag.ny()
                .leggTilBeregningsgrunnlagAktivitetStatus(new BeregningsgrunnlagAktivitetStatus(AktivitetStatus.FRILANSER))
                .leggTilBeregningsgrunnlagAktivitetStatus(new BeregningsgrunnlagAktivitetStatus(AktivitetStatus.ARBEIDSTAKER))
                .leggTilBeregningsgrunnlagPeriode(lagBeregningsgrunnlagPeriode())
                .build();

        // Act
        List<BeregningsgrunnlagRegel> regler = mapRegelListe(beregningsgrunnlag);

        // Assert
        assertThat(regler).hasSize(2);

        assertThat(regler.get(0).getAktivitetStatus()).isEqualTo(AktivitetStatus.FRILANSER);
        assertThat(regler.get(0).getAndelListe()).hasSize(1);
        assertThat(regler.get(0).getAndelListe().get(0).getDagsats()).isEqualTo(FRILANSER_DAGSATS);
        assertThat(regler.get(0).getAndelListe().get(0).getMånedsinntekt()).isEqualTo(FRILANSER_BRUTTO_PR_ÅR.divide(BigDecimal.valueOf(12), 0, RoundingMode.HALF_UP).longValue());
        assertThat(regler.get(0).getAndelListe().get(0).getÅrsinntekt()).isEqualTo(FRILANSER_BRUTTO_PR_ÅR.longValue());

        assertThat(regler.get(1).getAktivitetStatus()).isEqualTo(AktivitetStatus.ARBEIDSTAKER);
        assertThat(regler.get(1).getAndelListe()).hasSize(1);
        assertThat(regler.get(1).getAndelListe().get(0).getDagsats()).isEqualTo(ARBEIDSTAKER_DAGSATS);
        assertThat(regler.get(1).getAndelListe().get(0).getMånedsinntekt()).isEqualTo(ARBEIDSTAKER_BRUTTO_PR_ÅR.divide(BigDecimal.valueOf(12), 0, RoundingMode.HALF_UP).longValue());
        assertThat(regler.get(1).getAndelListe().get(0).getÅrsinntekt()).isEqualTo(ARBEIDSTAKER_BRUTTO_PR_ÅR.longValue());
    }

    @Test
    public void skal_finne_at_brutto_beregningsgrunnlag_er_brukt_fordi_det_er_mer_enn_en_regel() {
        // Arrange
        BeregningsgrunnlagRegel regel1 = BeregningsgrunnlagRegel.ny()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSAVKLARINGSPENGER)
                .build();
        BeregningsgrunnlagRegel regel2 = BeregningsgrunnlagRegel.ny()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .build();

        // Act
        boolean resultat = harBruktBruttoBeregningsgrunnlag(of(regel1, regel2));

        // Assert
        assertThat(resultat).isTrue();
    }

    @Test
    public void skal_finne_at_brutto_beregningsgrunnlag_er_brukt_fordi_det_er_kombinert_status() {
        // Arrange
        BeregningsgrunnlagRegel regel1 = BeregningsgrunnlagRegel.ny()
                .medAktivitetStatus(AktivitetStatus.KOMBINERT_AT_FL)
                .build();

        // Act
        boolean resultat = harBruktBruttoBeregningsgrunnlag(of(regel1));

        // Assert
        assertThat(resultat).isTrue();
    }

    @Test
    public void skal_finne_at_brutto_beregningsgrunnlag_ikke_er_brukt() {
        // Arrange
        BeregningsgrunnlagRegel regel1 = BeregningsgrunnlagRegel.ny()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .build();

        // Act
        boolean resultat = harBruktBruttoBeregningsgrunnlag(of(regel1));

        // Assert
        assertThat(resultat).isFalse();
    }

    private BeregningsgrunnlagPeriode lagBeregningsgrunnlagPeriode() {
        return BeregningsgrunnlagPeriode.ny()
                .medDagsats(STANDARD_PERIODE_DAGSATS)
                .medBruttoPrÅr(FRILANSER_BRUTTO_PR_ÅR)
                .medAvkortetPrÅr(AVKORTET_PR_ÅR)
                .medBeregningsgrunnlagPrStatusOgAndelList(lagBgpsaListe())
                .build();
    }

    private List<BeregningsgrunnlagPrStatusOgAndel> lagBgpsaListe() {
        return of(lagBgpsaBruttoFrilanser(), lagBgpsaAvkortetArbeidstaker());
    }

    private BeregningsgrunnlagPrStatusOgAndel lagBgpsaBruttoFrilanser() {
        return BeregningsgrunnlagPrStatusOgAndel.ny()
                .medBruttoPrÅr(FRILANSER_BRUTTO_PR_ÅR)
                .medDagsats(FRILANSER_DAGSATS)
                .medAktivitetStatus(AktivitetStatus.FRILANSER)
                .build();
    }

    private BeregningsgrunnlagPrStatusOgAndel lagBgpsaAvkortetArbeidstaker() {
        return BeregningsgrunnlagPrStatusOgAndel.ny()
                .medBruttoPrÅr(ARBEIDSTAKER_BRUTTO_PR_ÅR)
                .medAvkortetPrÅr(AVKORTET_PR_ÅR)
                .medDagsats(ARBEIDSTAKER_DAGSATS)
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .build();
    }
}