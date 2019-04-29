package no.nav.foreldrepenger.melding.dtomapper;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import no.nav.foreldrepenger.fpsak.dto.behandling.familiehendelse.AvklartDataAdopsjonDto;
import no.nav.foreldrepenger.fpsak.dto.behandling.familiehendelse.AvklartDataFodselDto;
import no.nav.foreldrepenger.fpsak.dto.behandling.familiehendelse.AvklartDataOmsorgDto;
import no.nav.foreldrepenger.fpsak.dto.behandling.familiehendelse.FamiliehendelseDto;
import no.nav.foreldrepenger.melding.familiehendelse.FamilieHendelse;
import no.nav.foreldrepenger.melding.familiehendelse.FamilieHendelseType;

@ApplicationScoped
public class FamiliehendelseDtoMapper {


    static BigInteger utledAntallBarnFraDto(FamiliehendelseDto familiehendelseDto) {
        if (familiehendelseDto instanceof AvklartDataAdopsjonDto) {
            return BigInteger.valueOf(((AvklartDataAdopsjonDto) familiehendelseDto).getAdopsjonFodelsedatoer().size());
        } else if (familiehendelseDto instanceof AvklartDataFodselDto) {
            return BigInteger.valueOf(utledAntallBarnFødsel((AvklartDataFodselDto) familiehendelseDto));
        } else if (familiehendelseDto instanceof AvklartDataOmsorgDto) {
            return BigInteger.valueOf(((AvklartDataOmsorgDto) familiehendelseDto).getAntallBarnTilBeregning());
        }
        throw new IllegalStateException("Familihendelse er av ukjent type");
    }

    static Optional<LocalDate> finnTermindato(FamiliehendelseDto dto) {
        if (dto instanceof AvklartDataFodselDto) {
            if (((AvklartDataFodselDto) dto).getTermindato() != null) {
                return Optional.of(((AvklartDataFodselDto) dto).getTermindato());
            }
        }
        return Optional.empty();
    }

    private static int utledAntallBarnFødsel(AvklartDataFodselDto familiehendelseDto) {
        int sum = 0;
        if (familiehendelseDto.getAntallBarnTermin() != null) {
            sum += familiehendelseDto.getAntallBarnTermin();
        }
        sum += familiehendelseDto.getAvklartBarn().size();
        return sum;
    }

    public FamilieHendelse mapFamiliehendelsefraDto(FamiliehendelseDto dto) {
        BigInteger antallBarnFraDto = utledAntallBarnFraDto(dto);
        Optional<LocalDate> termindatoFraDto = finnTermindato(dto);
        boolean barnErFødtFraDto = false;
        boolean gjelderFødsel = false;
        if (dto instanceof AvklartDataFodselDto) {
            barnErFødtFraDto = !((AvklartDataFodselDto) dto).getAvklartBarn().isEmpty();
            gjelderFødsel = true;
        }
        FamilieHendelseType familiehendelseType = mapFamiliehendelseType(dto);
        return new FamilieHendelse(antallBarnFraDto, termindatoFraDto, barnErFødtFraDto, gjelderFødsel, familiehendelseType);
    }

    private FamilieHendelseType mapFamiliehendelseType(FamiliehendelseDto dto) {
        if (dto instanceof AvklartDataAdopsjonDto) {
            return FamilieHendelseType.ADOPSJON;
        }
        if (dto instanceof AvklartDataOmsorgDto) {
            return FamilieHendelseType.OMSORG;
        }
        if (dto instanceof AvklartDataFodselDto) {
            return utledFødselEllerTermin((AvklartDataFodselDto) dto);
        }
        throw new IllegalStateException();
    }

    private FamilieHendelseType utledFødselEllerTermin(AvklartDataFodselDto dto) {
        if (dto.getAvklartBarn().isEmpty()) {
            return FamilieHendelseType.TERMIN;
        }
        return FamilieHendelseType.FØDSEL;
    }
}