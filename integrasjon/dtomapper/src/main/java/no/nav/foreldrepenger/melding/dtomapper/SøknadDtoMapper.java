package no.nav.foreldrepenger.melding.dtomapper;

import no.nav.foreldrepenger.fpsak.dto.soknad.SoknadDto;
import no.nav.foreldrepenger.melding.søknad.Søknad;

public class SøknadDtoMapper {
    public static Søknad mapSøknadFraDto(SoknadDto dto) {
        return new Søknad(dto.getMottattDato(), dto.getSoknadsdato());
    }
}
