package no.nav.foreldrepenger.fpformidling.personopplysning;

import no.nav.foreldrepenger.fpformidling.kodeverk.kodeverdi.Kodeverdi;

public enum NavBrukerKjønn implements Kodeverdi {

    KVINNE("K"),
    MANN("M"),
    UDEFINERT("-"),
    ;

    private String kode;

    NavBrukerKjønn() {
    }

    private NavBrukerKjønn(String kode) {
        this.kode = kode;
    }

    @Override
    public String getKode() {
        return kode;
    }

}
