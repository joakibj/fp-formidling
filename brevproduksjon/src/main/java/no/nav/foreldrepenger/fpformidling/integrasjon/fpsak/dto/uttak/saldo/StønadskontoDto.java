package no.nav.foreldrepenger.fpformidling.integrasjon.fpsak.dto.uttak.saldo;

import no.nav.foreldrepenger.fpformidling.uttak.SaldoVisningStønadskontoType;

public record StønadskontoDto(SaldoVisningStønadskontoType stonadskontotype, int maxDager, int saldo, KontoUtvidelser kontoUtvidelser) {

}
