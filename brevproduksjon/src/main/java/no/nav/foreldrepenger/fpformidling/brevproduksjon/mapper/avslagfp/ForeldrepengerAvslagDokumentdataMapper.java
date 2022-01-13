package no.nav.foreldrepenger.fpformidling.brevproduksjon.mapper.avslagfp;

import static no.nav.foreldrepenger.fpformidling.brevproduksjon.mapper.felles.BrevMapperUtil.opprettFellesBuilder;
import static no.nav.foreldrepenger.fpformidling.brevproduksjon.mapper.felles.MottattdokumentMapper.finnSøknadsdatoFraMottatteDokumenter;
import static no.nav.foreldrepenger.fpformidling.typer.Dato.formaterDato;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.fpformidling.behandling.Behandling;
import no.nav.foreldrepenger.fpformidling.beregning.BeregningsresultatFP;
import no.nav.foreldrepenger.fpformidling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.fpformidling.brevproduksjon.mapper.felles.BeregningsgrunnlagMapper;
import no.nav.foreldrepenger.fpformidling.brevproduksjon.mapper.felles.BrevParametere;
import no.nav.foreldrepenger.fpformidling.brevproduksjon.mapper.felles.DokumentdataMapper;
import no.nav.foreldrepenger.fpformidling.brevproduksjon.mapper.felles.Tuple;
import no.nav.foreldrepenger.fpformidling.brevproduksjon.tjenester.DomeneobjektProvider;
import no.nav.foreldrepenger.fpformidling.dokumentdata.DokumentFelles;
import no.nav.foreldrepenger.fpformidling.dokumentdata.DokumentMalTypeRef;
import no.nav.foreldrepenger.fpformidling.fagsak.FagsakBackend;
import no.nav.foreldrepenger.fpformidling.familiehendelse.FamilieHendelse;
import no.nav.foreldrepenger.fpformidling.hendelser.DokumentHendelse;
import no.nav.foreldrepenger.fpformidling.integrasjon.dokgen.dto.avslagfp.AvslåttPeriode;
import no.nav.foreldrepenger.fpformidling.integrasjon.dokgen.dto.avslagfp.ForeldrepengerAvslagDokumentdata;
import no.nav.foreldrepenger.fpformidling.integrasjon.dokgen.dto.felles.Fritekst;
import no.nav.foreldrepenger.fpformidling.kodeverk.kodeverdi.DokumentMalTypeKode;
import no.nav.foreldrepenger.fpformidling.mottattdokument.MottattDokument;
import no.nav.foreldrepenger.fpformidling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.fpformidling.uttak.UttakResultatPerioder;

@ApplicationScoped
@DokumentMalTypeRef(DokumentMalTypeKode.FORELDREPENGER_AVSLAG)
public class ForeldrepengerAvslagDokumentdataMapper implements DokumentdataMapper {

    private static final Map<RelasjonsRolleType, String> relasjonskodeTypeMap;
    static {
        relasjonskodeTypeMap = new HashMap<>();
        relasjonskodeTypeMap.put(RelasjonsRolleType.MORA, "MOR");
        relasjonskodeTypeMap.put(RelasjonsRolleType.FARA, "FAR");
        relasjonskodeTypeMap.put(RelasjonsRolleType.MEDMOR, "MEDMOR");
    }

    private BrevParametere brevParametere;
    private DomeneobjektProvider domeneobjektProvider;

    ForeldrepengerAvslagDokumentdataMapper() {
        //CDI
    }

    @Inject
    public ForeldrepengerAvslagDokumentdataMapper(BrevParametere brevParametere,
                                                  DomeneobjektProvider domeneobjektProvider) {
        this.brevParametere = brevParametere;
        this.domeneobjektProvider = domeneobjektProvider;
    }

    @Override
    public String getTemplateNavn() {
        return "foreldrepenger-avslag";
    }

    @Override
    public ForeldrepengerAvslagDokumentdata mapTilDokumentdata(DokumentFelles dokumentFelles, DokumentHendelse dokumentHendelse,
                                                               Behandling behandling, boolean erUtkast) {

        var fellesBuilder = opprettFellesBuilder(dokumentFelles, dokumentHendelse, behandling, erUtkast);
        fellesBuilder.medBrevDato(dokumentFelles.getDokumentDato() != null ? formaterDato(dokumentFelles.getDokumentDato(), behandling.getSpråkkode()) : null);
        fellesBuilder.medErAutomatiskBehandlet(dokumentFelles.getAutomatiskBehandlet());
        Fritekst.fra(dokumentHendelse, behandling).ifPresent(fellesBuilder::medFritekst);

        FagsakBackend fagsak = domeneobjektProvider.hentFagsakBackend(behandling);
        List<MottattDokument> mottatteDokumenter = domeneobjektProvider.hentMottatteDokumenter(behandling);
        FamilieHendelse familiehendelse = domeneobjektProvider.hentFamiliehendelse(behandling);
        Optional<Beregningsgrunnlag> beregningsgrunnlagOpt = domeneobjektProvider.hentBeregningsgrunnlagHvisFinnes(behandling);
        long halvG = BeregningsgrunnlagMapper.getHalvGOrElseZero(beregningsgrunnlagOpt);
        Optional<UttakResultatPerioder> uttakResultatPerioder = domeneobjektProvider.hentUttaksresultatHvisFinnes(behandling);

        var dokumentdataBuilder = ForeldrepengerAvslagDokumentdata.ny()
                .medFelles(fellesBuilder.build())
                .medRelasjonskode(finnRelasjonskode(fagsak))
                .medMottattDato(formaterDato(finnSøknadsdatoFraMottatteDokumenter(behandling, mottatteDokumenter), behandling.getSpråkkode()))
                .medGjelderFødsel(familiehendelse.isGjelderFødsel())
                .medBarnErFødt(familiehendelse.isBarnErFødt())
                .medAnnenForelderHarRett(uttakResultatPerioder.map(UttakResultatPerioder::isAnnenForelderHarRett).orElse(false))
                .medAntallBarn(familiehendelse.getAntallBarn().intValue())
                .medHalvG(halvG)
                .medKlagefristUker(brevParametere.getKlagefristUker())
                .medKreverSammenhengendeUttak(domeneobjektProvider.kreverSammenhengendeUttak(behandling));

        mapAvslåttePerioder(behandling, dokumentdataBuilder, uttakResultatPerioder);

        return dokumentdataBuilder.build();
    }

    private void mapAvslåttePerioder(Behandling behandling, ForeldrepengerAvslagDokumentdata.Builder dokumentdataBuilder,
                                     Optional<UttakResultatPerioder> uttakResultatPerioder) {
        Optional<BeregningsresultatFP> beregningsresultatFP = domeneobjektProvider.hentBeregningsresultatFPHvisFinnes(behandling);
        Tuple<List<AvslåttPeriode>, String> avslåttePerioderOgLovhjemmel = AvslåttPeriodeMapper.mapAvslåttePerioderOgLovhjemmel(
                behandling,
                beregningsresultatFP.map(BeregningsresultatFP::getBeregningsresultatPerioder).orElse(Collections.emptyList()),
                uttakResultatPerioder);

        dokumentdataBuilder.medLovhjemmelForAvslag(avslåttePerioderOgLovhjemmel.element2());
        dokumentdataBuilder.medAvslåttePerioder(avslåttePerioderOgLovhjemmel.element1());
    }

    private String finnRelasjonskode(FagsakBackend fagsak) {
        if (RelasjonsRolleType.erRegistrertForeldre(fagsak.getRelasjonsRolleType())) {
            return relasjonskodeTypeMap.get(fagsak.getRelasjonsRolleType());
        }
        return "ANNET";
    }
}