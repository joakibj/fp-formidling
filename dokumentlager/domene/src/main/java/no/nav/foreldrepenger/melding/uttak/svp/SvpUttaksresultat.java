package no.nav.foreldrepenger.melding.uttak.svp;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SvpUttaksresultat {

    private List<SvpUttakResultatArbeidsforhold> uttakResultatArbeidsforhold;
    private List<SvpUttakResultatPerioder> uttakPerioder;

    private List<SvpUttakResultatPeriode> avslagPerioder;

    public List<SvpUttakResultatPerioder> getUttakPerioder() {
        return uttakPerioder;
    }

    public List<SvpUttakResultatPeriode> getAvslagPerioder() {
        return avslagPerioder;
    }

    private SvpUttaksresultat(Builder builder) {
        uttakResultatArbeidsforhold = builder.uttakResultatArbeidsforhold;
        uttakPerioder = builder.uttakResultatPerioder;
        avslagPerioder = builder.avslåttePerioder;
    }

    public static Builder ny() {
        return new Builder();
    }

    public List<SvpUttakResultatArbeidsforhold> getUttakResultatArbeidsforhold() {
        return uttakResultatArbeidsforhold.stream().sorted(Comparator.comparing(o -> o.getPerioder().get(0)))
                .collect(Collectors.toList());
    }


    public static final class Builder {
        private List<SvpUttakResultatArbeidsforhold> uttakResultatArbeidsforhold = new ArrayList<>();
        private List<SvpUttakResultatPerioder> uttakResultatPerioder = new ArrayList();
        private List<SvpUttakResultatPeriode> avslåttePerioder = new ArrayList();

        public Builder() {
        }

        public Builder medUttakResultatArbeidsforhold(SvpUttakResultatArbeidsforhold uttakResultatArbeidsforhold) {
            this.uttakResultatArbeidsforhold.add(uttakResultatArbeidsforhold);
            return this;
        }

        public SvpUttaksresultat build() {
            return new SvpUttaksresultat(this);
        }

        public Builder medUttakResultatPerioder(SvpUttakResultatPerioder uttakResultatPerioder) {
            this.uttakResultatPerioder.add(uttakResultatPerioder);
            return this;
        }

        public Builder medAvslåttePerioder(List<SvpUttakResultatPeriode> avslåttePerioder) {
            this.avslåttePerioder = avslåttePerioder;
            return this;
        }
    }
}