package no.nav.foreldrepenger.fpformidling.integrasjon.dokgen.dto.innvilgelsesvp;

import static no.nav.foreldrepenger.fpformidling.typer.Dato.formaterDato;

import java.time.LocalDate;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;

import no.nav.foreldrepenger.fpformidling.geografisk.Språkkode;
import no.nav.foreldrepenger.fpformidling.integrasjon.dokgen.dto.felles.Årsak;

@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Avslagsperiode {
    private String periodeFom;
    @JsonIgnore
    private LocalDate periodeFomDate;
    private String periodeTom;
    @JsonIgnore
    private LocalDate periodeTomDate;
    private Årsak årsak;
    @JsonIgnore
    private Språkkode språkkode;

    public LocalDate getPeriodeFom() {
        return periodeFomDate;
    }

    public LocalDate getPeriodeTom() {
        return periodeTomDate;
    }

    public Årsak getÅrsak() {
        return årsak;
    }

    public Språkkode getSpråkkode() {
        return språkkode;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        var that = (Avslagsperiode) object;
        return Objects.equals(periodeFom, that.periodeFom) && Objects.equals(periodeTom, that.periodeTom) && Objects.equals(årsak, that.årsak);
    }

    @Override
    public int hashCode() {
        return Objects.hash(periodeFom, periodeTom, årsak);
    }

    public static Builder ny() {
        return new Builder();
    }

    public static Builder ny(Avslagsperiode eksisterende) {
        return new Builder(eksisterende);
    }

    public static class Builder {
        private Avslagsperiode kladd;

        private Builder() {
            this.kladd = new Avslagsperiode();
        }

        private Builder(Avslagsperiode eksisterende) {
            this.kladd = new Avslagsperiode();
            this.kladd.periodeFom = formaterDato(eksisterende.getPeriodeFom(), eksisterende.getSpråkkode());
            this.kladd.periodeFomDate = eksisterende.getPeriodeFom();
            this.kladd.periodeTom = formaterDato(eksisterende.getPeriodeTom(), eksisterende.getSpråkkode());
            this.kladd.periodeTomDate = eksisterende.getPeriodeTom();
            this.kladd.årsak = eksisterende.getÅrsak();
            this.kladd.språkkode = eksisterende.getSpråkkode();
        }

        public Builder medPeriodeFom(LocalDate periodeFom, Språkkode språkkode) {
            this.kladd.periodeFom = formaterDato(periodeFom, språkkode);
            this.kladd.periodeFomDate = periodeFom;
            this.kladd.språkkode = språkkode;
            return this;
        }

        public Builder medPeriodeTom(LocalDate periodeTom, Språkkode språkkode) {
            this.kladd.periodeTom = formaterDato(periodeTom, språkkode);
            this.kladd.periodeTomDate = periodeTom;
            this.kladd.språkkode = språkkode;
            return this;
        }

        public Builder medÅrsak(Årsak årsak) {
            this.kladd.årsak = årsak;
            return this;
        }

        public Avslagsperiode build() {
            return this.kladd;
        }
    }
}
