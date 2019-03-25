package no.nav.foreldrepenger.melding.brevbestiller;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.melding.datamapper.DokumentBestillerTjenesteUtil;
import no.nav.foreldrepenger.melding.dokumentdata.DokumentFelles;
import no.nav.foreldrepenger.melding.dokumentdata.DokumentMalType;
import no.nav.foreldrepenger.melding.fagsak.Fagsystem;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.informasjon.Adresse;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.informasjon.Dokumentbestillingsinformasjon;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.informasjon.Fagomraader;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.informasjon.Fagsystemer;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.informasjon.Person;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.informasjon.UtenlandskPostadresse;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
public class DokumentbestillingMapper {

    private static final String FAGOMRÅDE_KODE = "FOR";
    private static final String JOURNALFØRENDE_ENHET_KODE = "9999";
    private static final String UKJENT_ADRESSE = "Ukjent adresse";
    private LandkodeOversetter landkodeOversetter;
    private String testFnr;

    public DokumentbestillingMapper() {
        //CDI
    }

    @Inject
    public DokumentbestillingMapper(LandkodeOversetter landkodeOversetter,
                                    @KonfigVerdi(value = "test.fnr") String testFnr) {
        this.landkodeOversetter = landkodeOversetter;
        this.testFnr = testFnr;
    }

    public Dokumentbestillingsinformasjon mapFraBehandling(DokumentMalType dokumentMal, DokumentFelles dokumentFelles, boolean harVedlegg) {
        final Dokumentbestillingsinformasjon dokumentbestillingsinformasjon = new Dokumentbestillingsinformasjon();
        dokumentbestillingsinformasjon.setDokumenttypeId(dokumentMal.getDoksysKode());
        Fagsystemer vlfp = new Fagsystemer();
        vlfp.setKodeRef(Fagsystem.FPSAK.getOffisiellKode());
        vlfp.setValue(Fagsystem.FPSAK.getOffisiellKode());
        dokumentbestillingsinformasjon.setBestillendeFagsystem(vlfp);
        setPostadresse(dokumentFelles, dokumentbestillingsinformasjon);
        Person bruker = new Person();
        bruker.setIdent(testFnr);
        bruker.setNavn(dokumentFelles.getSakspartNavn());
        dokumentbestillingsinformasjon.setBruker(bruker);
        Fagomraader dokumenttilhørendeFagområde = new Fagomraader();
        dokumenttilhørendeFagområde.setKodeRef(FAGOMRÅDE_KODE);
        dokumenttilhørendeFagområde.setValue(FAGOMRÅDE_KODE);
        dokumentbestillingsinformasjon.setDokumenttilhoerendeFagomraade(dokumenttilhørendeFagområde);
        dokumentbestillingsinformasjon.setFerdigstillForsendelse(!harVedlegg);
        dokumentbestillingsinformasjon.setInkludererEksterneVedlegg(harVedlegg);
        dokumentbestillingsinformasjon.setJournalfoerendeEnhet(JOURNALFØRENDE_ENHET_KODE); // FIXME (ONYX): (bts) bruke 9999 hvis automatisk, ellers pålogget saksbehandlers enhetskode
        dokumentbestillingsinformasjon.setJournalsakId(dokumentFelles.getSaksnummer().getVerdi());
        Person mottaker = new Person();
        mottaker.setIdent(testFnr);
        mottaker.setNavn(dokumentFelles.getMottakerNavn());
        dokumentbestillingsinformasjon.setMottaker(mottaker);
        dokumentbestillingsinformasjon.setSaksbehandlernavn(dokumentFelles.getSignerendeBeslutterNavn() == null ? "Vedtaksløsning Prosess" : dokumentFelles.getSignerendeBeslutterNavn());
        Fagsystemer gsak = new Fagsystemer();
        gsak.setKodeRef(Fagsystem.GOSYS.getOffisiellKode());
        gsak.setValue(Fagsystem.GOSYS.getOffisiellKode());
        dokumentbestillingsinformasjon.setSakstilhoerendeFagsystem(gsak);
        return dokumentbestillingsinformasjon;
    }

    private void setPostadresse(DokumentFelles dokumentFelles, Dokumentbestillingsinformasjon dokumentbestillingsinformasjon) {
        Adresse adresse;
        if (DokumentBestillerTjenesteUtil.erNorskAdresse(dokumentFelles.getMottakerAdresse())) {
            adresse = DokumentBestillerTjenesteUtil.lagNorskPostadresse(dokumentFelles);
        } else {
            adresse = lagUtenlandskPostadresse(dokumentFelles);
        }
        dokumentbestillingsinformasjon.setAdresse(adresse);
    }

    private UtenlandskPostadresse lagUtenlandskPostadresse(DokumentFelles dokumentFelles) {
        UtenlandskPostadresse adresse = new UtenlandskPostadresse();
        adresse.setAdresselinje1(dokumentFelles.getMottakerAdresse().getAdresselinje1() == null ? UKJENT_ADRESSE : dokumentFelles.getMottakerAdresse().getAdresselinje1());
        adresse.setAdresselinje2(dokumentFelles.getMottakerAdresse().getAdresselinje2());
        adresse.setAdresselinje3(dokumentFelles.getMottakerAdresse().getAdresselinje3());
        no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.informasjon.Landkoder landkode = new no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.informasjon.Landkoder();
        landkode.setValue(landkodeOversetter.tilIso2(dokumentFelles.getMottakerAdresse().getLand()));
        adresse.setLand(landkode);
        return adresse;
    }
}