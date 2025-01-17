package no.nav.foreldrepenger.fpformidling.brevproduksjon.mapper.innvilgelsesvp;

import static no.nav.foreldrepenger.fpformidling.behandling.KonsekvensForYtelsen.ENDRING_I_BEREGNING;
import static no.nav.foreldrepenger.fpformidling.brevproduksjon.mapper.felles.BehandlingMapper.erEndretFraAvslått;
import static no.nav.foreldrepenger.fpformidling.brevproduksjon.mapper.felles.BehandlingMapper.erRevurderingPgaEndretBeregningsgrunnlag;
import static no.nav.foreldrepenger.fpformidling.brevproduksjon.mapper.felles.BehandlingMapper.erTermindatoEndret;
import static no.nav.foreldrepenger.fpformidling.brevproduksjon.mapper.felles.BrevMapperUtil.opprettFellesBuilder;
import static no.nav.foreldrepenger.fpformidling.brevproduksjon.mapper.felles.MottattdokumentMapper.finnSisteMottatteSøknad;
import static no.nav.foreldrepenger.fpformidling.brevproduksjon.mapper.felles.TilkjentYtelseMapper.finnAntallRefusjonerTilArbeidsgivere;
import static no.nav.foreldrepenger.fpformidling.brevproduksjon.mapper.felles.TilkjentYtelseMapper.finnMånedsbeløp;
import static no.nav.foreldrepenger.fpformidling.brevproduksjon.mapper.felles.TilkjentYtelseMapper.harBrukerAndel;
import static no.nav.foreldrepenger.fpformidling.brevproduksjon.mapper.innvilgelsesvp.AvslagsperiodeMapper.mapAvslagsperioder;
import static no.nav.foreldrepenger.fpformidling.brevproduksjon.mapper.innvilgelsesvp.AvslåttAktivitetMapper.mapAvslåtteAktiviteter;
import static no.nav.foreldrepenger.fpformidling.brevproduksjon.mapper.innvilgelsesvp.BeregningMapper.erMilitærSivil;
import static no.nav.foreldrepenger.fpformidling.brevproduksjon.mapper.innvilgelsesvp.BeregningMapper.finnSeksG;
import static no.nav.foreldrepenger.fpformidling.brevproduksjon.mapper.innvilgelsesvp.BeregningMapper.getAvkortetPrÅrSVP;
import static no.nav.foreldrepenger.fpformidling.brevproduksjon.mapper.innvilgelsesvp.BeregningMapper.inntektOverSeksG;
import static no.nav.foreldrepenger.fpformidling.brevproduksjon.mapper.innvilgelsesvp.BeregningMapper.mapArbeidsforhold;
import static no.nav.foreldrepenger.fpformidling.brevproduksjon.mapper.innvilgelsesvp.BeregningMapper.mapFrilanser;
import static no.nav.foreldrepenger.fpformidling.brevproduksjon.mapper.innvilgelsesvp.BeregningMapper.mapSelvstendigNæringsdrivende;
import static no.nav.foreldrepenger.fpformidling.brevproduksjon.mapper.innvilgelsesvp.BeregningMapper.utledLovhjemmelForBeregning;
import static no.nav.foreldrepenger.fpformidling.brevproduksjon.mapper.innvilgelsesvp.NaturalytelseMapper.mapNaturalytelser;
import static no.nav.foreldrepenger.fpformidling.brevproduksjon.mapper.innvilgelsesvp.UtbetalingsperiodeMapper.finnStønadsperiodeTom;
import static no.nav.foreldrepenger.fpformidling.brevproduksjon.mapper.innvilgelsesvp.UtbetalingsperiodeMapper.mapUtbetalingsperioder;
import static no.nav.foreldrepenger.fpformidling.brevproduksjon.mapper.innvilgelsesvp.UttaksperiodeMapper.mapUttaksaktivteterMedPerioder;
import static no.nav.foreldrepenger.fpformidling.typer.Dato.formaterDato;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.fpformidling.behandling.Behandling;
import no.nav.foreldrepenger.fpformidling.brevproduksjon.mapper.felles.BrevParametere;
import no.nav.foreldrepenger.fpformidling.brevproduksjon.mapper.felles.DokumentdataMapper;
import no.nav.foreldrepenger.fpformidling.brevproduksjon.tjenester.DomeneobjektProvider;
import no.nav.foreldrepenger.fpformidling.dokumentdata.DokumentFelles;
import no.nav.foreldrepenger.fpformidling.dokumentdata.DokumentMalTypeRef;
import no.nav.foreldrepenger.fpformidling.hendelser.DokumentHendelse;
import no.nav.foreldrepenger.fpformidling.integrasjon.dokgen.dto.felles.Beløp;
import no.nav.foreldrepenger.fpformidling.integrasjon.dokgen.dto.felles.FritekstDto;
import no.nav.foreldrepenger.fpformidling.integrasjon.dokgen.dto.innvilgelsesvp.SvangerskapspengerInnvilgelseDokumentdata;
import no.nav.foreldrepenger.fpformidling.integrasjon.dokgen.dto.innvilgelsesvp.Uttaksaktivitet;
import no.nav.foreldrepenger.fpformidling.kodeverk.kodeverdi.DokumentMalTypeKode;

@ApplicationScoped
@DokumentMalTypeRef(DokumentMalTypeKode.SVANGERSKAPSPENGER_INNVILGELSE)
public class SvangerskapspengerInnvilgelseDokumentdataMapper implements DokumentdataMapper {

    private DomeneobjektProvider domeneobjektProvider;
    private BrevParametere brevParametere;

    SvangerskapspengerInnvilgelseDokumentdataMapper() {
        //CDI
    }

    @Inject
    public SvangerskapspengerInnvilgelseDokumentdataMapper(DomeneobjektProvider domeneobjektProvider, BrevParametere brevParametere) {
        this.domeneobjektProvider = domeneobjektProvider;
        this.brevParametere = brevParametere;
    }

    private static boolean erNyEllerEndretBeregning(Behandling behandling) {
        return behandling.erFørstegangssøknad() || behandling.getBehandlingsresultat().getKonsekvenserForYtelsen().contains(ENDRING_I_BEREGNING);
    }

    @Override
    public String getTemplateNavn() {
        return "svangerskapspenger-innvilgelse";
    }

    @Override
    public SvangerskapspengerInnvilgelseDokumentdata mapTilDokumentdata(DokumentFelles dokumentFelles,
                                                                        DokumentHendelse hendelse,
                                                                        Behandling behandling,
                                                                        boolean erUtkast) {
        var språkkode = behandling.getSpråkkode();
        var mottatteDokumenter = domeneobjektProvider.hentMottatteDokumenter(behandling);
        var beregningsgrunnlag = domeneobjektProvider.hentBeregningsgrunnlag(behandling);
        var tilkjentYtelse = domeneobjektProvider.hentTilkjentYtelseForeldrepenger(behandling);
        var uttaksresultatSvp = domeneobjektProvider.hentSvangerskapspengerUttak(behandling);

        var fellesBuilder = opprettFellesBuilder(dokumentFelles, hendelse, behandling, erUtkast);
        fellesBuilder.medBrevDato(dokumentFelles.getDokumentDato() != null ? formaterDato(dokumentFelles.getDokumentDato(), språkkode) : null);
        FritekstDto.fra(hendelse, behandling).ifPresent(fellesBuilder::medFritekst);

        var utbetalingsperioder = mapUtbetalingsperioder(tilkjentYtelse.getPerioder(), språkkode);
        var uttaksaktiviteter = mapUttaksaktivteterMedPerioder(uttaksresultatSvp, tilkjentYtelse, språkkode);
        var inkludereBeregning = erNyEllerEndretBeregning(behandling);

        var dokumentdataBuilder = SvangerskapspengerInnvilgelseDokumentdata.ny()
            .medFelles(fellesBuilder.build())
            .medRevurdering(behandling.erRevurdering())
            .medRefusjonTilBruker(harBrukerAndel(tilkjentYtelse))
            .medAntallRefusjonerTilArbeidsgivere(finnAntallRefusjonerTilArbeidsgivere(tilkjentYtelse))
            .medStønadsperiodeTom(formaterDato(finnStønadsperiodeTom(utbetalingsperioder), språkkode))
            .medMånedsbeløp(finnMånedsbeløp(tilkjentYtelse))
            .medMottattDato(formaterDato(finnSisteMottatteSøknad(mottatteDokumenter), språkkode))
            .medKlagefristUker(brevParametere.getKlagefristUker())
            .medAntallUttaksperioder(tellAntallUttaksperioder(uttaksaktiviteter))
            .medUttaksaktiviteter(uttaksaktiviteter)
            .medUtbetalingsperioder(utbetalingsperioder)
            .medAvslagsperioder(mapAvslagsperioder(uttaksresultatSvp.getUttakResultatArbeidsforhold(), språkkode))
            .medAvslåtteAktiviteter(mapAvslåtteAktiviteter(uttaksresultatSvp.getUttakResultatArbeidsforhold()))
            .medInkludereBeregning(inkludereBeregning);

        if (behandling.erRevurdering()) {
            var orginalBehandling = domeneobjektProvider.hentOriginalBehandlingHvisFinnes(behandling);
            var originalFamiliehendelse = orginalBehandling.map(domeneobjektProvider::hentFamiliehendelse);
            var familieHendelse = domeneobjektProvider.hentFamiliehendelse(behandling);

            dokumentdataBuilder.medEndretFraAvslag(erEndretFraAvslått(orginalBehandling));
            dokumentdataBuilder.medUtbetalingEndret(erRevurderingPgaEndretBeregningsgrunnlag(behandling));
            dokumentdataBuilder.medTermindatoEndret(erTermindatoEndret(familieHendelse, originalFamiliehendelse));
        }

        if (inkludereBeregning) {
            //Spesielhåndtering av militærstatus. Dersom personen har militærstatus med dagsats er beregningsgrunnlaget satt til
            //3G. I disse tilfellene skal andre statuser ignoreres (Beregning fjerner ikke andre statuser). Gjelder både FP og SVP.
            if (erMilitærSivil(beregningsgrunnlag)) {
                dokumentdataBuilder.medMilitærSivil(true);
            } else {
                dokumentdataBuilder.medArbeidsforhold(mapArbeidsforhold(beregningsgrunnlag));
                dokumentdataBuilder.medSelvstendigNæringsdrivende(mapSelvstendigNæringsdrivende(beregningsgrunnlag));
                dokumentdataBuilder.medFrilanser(mapFrilanser(beregningsgrunnlag));
                dokumentdataBuilder.medMilitærSivil(false);
            }
            dokumentdataBuilder.medNaturalytelser(mapNaturalytelser(tilkjentYtelse, beregningsgrunnlag, språkkode));
            dokumentdataBuilder.medBruttoBeregningsgrunnlag(Beløp.of(getAvkortetPrÅrSVP(beregningsgrunnlag)));
            dokumentdataBuilder.medInntektOver6G(inntektOverSeksG(beregningsgrunnlag));
            dokumentdataBuilder.medSeksG(finnSeksG(beregningsgrunnlag).longValue());
            dokumentdataBuilder.medLovhjemmel(utledLovhjemmelForBeregning(beregningsgrunnlag, behandling));
        }

        return dokumentdataBuilder.build();
    }

    private int tellAntallUttaksperioder(List<Uttaksaktivitet> uttaksaktiviteter) {
        return uttaksaktiviteter.stream().mapToInt(aktivitet -> aktivitet.getUttaksperioder().size()).sum();
    }
}
