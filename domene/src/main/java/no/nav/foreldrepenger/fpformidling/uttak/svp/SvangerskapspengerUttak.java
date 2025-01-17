package no.nav.foreldrepenger.fpformidling.uttak.svp;

import no.nav.foreldrepenger.fpformidling.typer.DatoIntervall;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import static no.nav.vedtak.konfig.Tid.TIDENES_ENDE;

public class SvangerskapspengerUttak {

    private final List<SvpUttakResultatArbeidsforhold> uttakResultatArbeidsforhold;

    private SvangerskapspengerUttak(Builder builder) {
        uttakResultatArbeidsforhold = builder.uttakResultatArbeidsforhold;
    }

    public List<SvpUttakResultatArbeidsforhold> getUttakResultatArbeidsforhold() {
        return uttakResultatArbeidsforhold.stream().sorted(Comparator.comparing(getSammenligningsDato())).toList();
    }

    private Function<SvpUttakResultatArbeidsforhold, DatoIntervall> getSammenligningsDato() {
        return o -> !o.getPerioder().isEmpty() ? o.getPerioder().get(0).getTidsperiode() : DatoIntervall.fraOgMed(TIDENES_ENDE);
    }

    public static final class Builder {
        private final List<SvpUttakResultatArbeidsforhold> uttakResultatArbeidsforhold = new ArrayList<>();

        private Builder() {
            // Skjul default constructor
        }

        public static Builder ny() {
            return new Builder();
        }

        public Builder leggTilUttakResultatArbeidsforhold(SvpUttakResultatArbeidsforhold uttakResultatArbeidsforhold) {
            this.uttakResultatArbeidsforhold.add(uttakResultatArbeidsforhold);
            return this;
        }

        public SvangerskapspengerUttak build() {
            return new SvangerskapspengerUttak(this);
        }
    }
}
