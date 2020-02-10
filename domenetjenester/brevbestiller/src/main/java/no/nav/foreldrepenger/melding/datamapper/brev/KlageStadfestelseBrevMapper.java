package no.nav.foreldrepenger.melding.datamapper.brev;

import no.nav.foreldrepenger.melding.behandling.Behandling;
import no.nav.foreldrepenger.melding.datamapper.DomeneobjektProvider;
import no.nav.foreldrepenger.melding.datamapper.domene.KlageMapper;
import no.nav.foreldrepenger.melding.datamapper.konfig.BrevParametere;
import no.nav.foreldrepenger.melding.dokumentdata.DokumentMalType;
import no.nav.foreldrepenger.melding.hendelser.DokumentHendelse;
import no.nav.foreldrepenger.melding.klage.Klage;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Optional;

@ApplicationScoped
@Named(DokumentMalType.KLAGE_STADFESTET)
public class KlageStadfestelseBrevMapper extends FritekstmalBrevMapper {

    public KlageStadfestelseBrevMapper() {
        //CDI
    }

    @Inject
    public KlageStadfestelseBrevMapper(BrevParametere brevParametere, DomeneobjektProvider domeneobjektProvider) {
        super(brevParametere, domeneobjektProvider);
    }

    @Override
    public String displayName() {
        return "Vedtak om stadfestelse i klagesak";
    }

    @Override
    public String templateFolder() {
        return "vedtakomstadfestelseiklagesak";
    }

    @Override
    Brevdata mapTilBrevfelter(DokumentHendelse hendelse, Behandling behandling) {

        Brevdata brevdata = new Brevdata()
                .leggTil("saksbehandler", behandling.getAnsvarligSaksbehandler())
                .leggTil("medunderskriver", behandling.getAnsvarligBeslutter())
                .leggTil("ytelseType", hendelse.getYtelseType().getKode());

        Klage klage = domeneobjektProvider.hentKlagebehandling(behandling);
        Optional<String> fritekstOpt = KlageMapper.avklarFritekstKlage(hendelse, klage);
        if (fritekstOpt.isPresent()) {
            brevdata.leggTil("mintekst", fritekstOpt.get());
        }
        return brevdata;
    }
}



