package no.nav.foreldrepenger.fpformidling.brevproduksjon.mapper;

import static no.nav.foreldrepenger.fpformidling.brevproduksjon.mapper.felles.BrevMapperUtil.erDød;
import static no.nav.foreldrepenger.fpformidling.brevproduksjon.mapper.felles.BrevMapperUtil.formaterBeløp;
import static no.nav.foreldrepenger.fpformidling.brevproduksjon.mapper.felles.BrevMapperUtil.opprettFellesBuilder;
import static no.nav.foreldrepenger.fpformidling.typer.Dato.formaterDato;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.fpformidling.behandling.Behandling;
import no.nav.foreldrepenger.fpformidling.behandling.BehandlingType;
import no.nav.foreldrepenger.fpformidling.brevproduksjon.mapper.felles.BehandlingMapper;
import no.nav.foreldrepenger.fpformidling.brevproduksjon.mapper.felles.BrevParametere;
import no.nav.foreldrepenger.fpformidling.brevproduksjon.mapper.felles.DokumentdataMapper;
import no.nav.foreldrepenger.fpformidling.brevproduksjon.tjenester.DomeneobjektProvider;
import no.nav.foreldrepenger.fpformidling.dokumentdata.DokumentFelles;
import no.nav.foreldrepenger.fpformidling.dokumentdata.DokumentMalTypeRef;
import no.nav.foreldrepenger.fpformidling.familiehendelse.FamilieHendelse;
import no.nav.foreldrepenger.fpformidling.hendelser.DokumentHendelse;
import no.nav.foreldrepenger.fpformidling.integrasjon.dokgen.dto.EngangsstønadInnvilgelseDokumentdata;
import no.nav.foreldrepenger.fpformidling.kodeverk.kodeverdi.BehandlingÅrsakType;
import no.nav.foreldrepenger.fpformidling.kodeverk.kodeverdi.DokumentMalTypeKode;
import no.nav.foreldrepenger.fpformidling.tilkjentytelse.TilkjentYtelseEngangsstønad;

@ApplicationScoped
@DokumentMalTypeRef(DokumentMalTypeKode.ENGANGSSTØNAD_INNVILGELSE)
public class EngangsstønadInnvilgelseDokumentdataMapper implements DokumentdataMapper {

    private BrevParametere brevParametere;
    private DomeneobjektProvider domeneobjektProvider;

    EngangsstønadInnvilgelseDokumentdataMapper() {
        //CDI
    }

    @Inject
    public EngangsstønadInnvilgelseDokumentdataMapper(BrevParametere brevParametere, DomeneobjektProvider domeneobjektProvider) {
        this.brevParametere = brevParametere;
        this.domeneobjektProvider = domeneobjektProvider;
    }

    @Override
    public String getTemplateNavn() {
        return "engangsstonad-innvilgelse";
    }

    @Override
    public EngangsstønadInnvilgelseDokumentdata mapTilDokumentdata(DokumentFelles dokumentFelles,
                                                                   DokumentHendelse hendelse,
                                                                   Behandling behandling,
                                                                   boolean erUtkast) {
        var tilkjentYtelse = domeneobjektProvider.hentTilkjentYtelseEngangsstønad(behandling);

        var fellesBuilder = opprettFellesBuilder(dokumentFelles, hendelse, behandling, erUtkast);
        fellesBuilder.medBrevDato(
            dokumentFelles.getDokumentDato() != null ? formaterDato(dokumentFelles.getDokumentDato(), behandling.getSpråkkode()) : null);
        fellesBuilder.medErAutomatiskBehandlet(dokumentFelles.getAutomatiskBehandlet());

        var dokumentdataBuilder = EngangsstønadInnvilgelseDokumentdata.ny()
            .medFelles(fellesBuilder.build())
            .medRevurdering(behandling.erRevurdering())
            .medFørstegangsbehandling(behandling.erFørstegangssøknad())
            .medMedhold(BehandlingMapper.erMedhold(behandling))
            .medInnvilgetBeløp(formaterBeløp(tilkjentYtelse.beløp()))
            .medKlagefristUker(brevParametere.getKlagefristUker())
            .medDød(erDød(dokumentFelles))
            .medFbEllerMedhold(erFBellerMedhold(behandling))
            .medErEndretSats(false);

        if (behandling.erRevurdering()) {
            var originalBehandling = domeneobjektProvider.hentOriginalBehandlingHvisFinnes(behandling)
                .orElseThrow(() -> new IllegalArgumentException("Utviklerfeil:Finner ikke informasjon om orginal behandling for revurdering "));

            var differanse = sjekkOmDifferanseHvisRevurdering(originalBehandling, tilkjentYtelse);

            if (differanse != 0L) {
                var famHendelse = domeneobjektProvider.hentFamiliehendelse(behandling);
                var orgFamHendelse = domeneobjektProvider.hentFamiliehendelse(originalBehandling);
                //dersom årsaken til differanse er økning av antall barn er det ikke endret sats
                if (!antallBarnEndret(famHendelse, orgFamHendelse)) {
                    dokumentdataBuilder.medErEndretSats(true);
                    dokumentdataBuilder.medInnvilgetBeløp(formaterBeløp(differanse));
                } else {
                    dokumentdataBuilder.medInnvilgetBeløp(formaterBeløp(differanse));
                }
            }
        }
        return dokumentdataBuilder.build();
    }

    private boolean erFBellerMedhold(Behandling behandling) {
        return !BehandlingType.REVURDERING.equals(behandling.getBehandlingType()) || BehandlingÅrsakType.årsakerEtterKlageBehandling()
            .stream()
            .anyMatch(behandling::harBehandlingÅrsak);
    }

    private Long sjekkOmDifferanseHvisRevurdering(Behandling originalBehandling, TilkjentYtelseEngangsstønad tilkjentYtelse) {
        var originaltTilkjentYtelse = domeneobjektProvider.hentTilkjentYtelseESHvisFinnes(originalBehandling);

        return originaltTilkjentYtelse.map(orgTilkjentYtelse -> Math.abs(tilkjentYtelse.beløp() - orgTilkjentYtelse.beløp())).orElse(0L);
    }

    private boolean antallBarnEndret(FamilieHendelse famHendelse, FamilieHendelse orgFamhendelse) {
        return famHendelse.antallBarn() != orgFamhendelse.antallBarn();
    }
}
