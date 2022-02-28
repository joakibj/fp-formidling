package no.nav.foreldrepenger.fpsak.mapper;

import static no.nav.foreldrepenger.fpformidling.behandling.Behandlingsresultat.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.fpformidling.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.fpformidling.behandling.KonsekvensForYtelsen;
import no.nav.foreldrepenger.fpsak.dto.behandling.BehandlingsresultatDto;

public class BehandlingsresultatDtoMapper {


    public static Behandlingsresultat mapBehandlingsresultatFraDto(BehandlingsresultatDto dto) {
        Behandlingsresultat.Builder builder = builder();
        if (dto.getAvslagsarsak() != null) {
            builder.medAvslagsårsak(dto.getAvslagsarsak());
        }
        if (dto.getType() != null) {
            builder.medBehandlingResultatType(dto.getType());
        }
        builder.medFritekstbrev(dto.getFritekstbrev())
                .medOverskrift(dto.getOverskrift())
                .medVedtaksbrev(dto.getVedtaksbrev())
                .medAvslagarsakFritekst(dto.getAvslagsarsakFritekst());
        List<KonsekvensForYtelsen> konsekvenserForYtelsen = new ArrayList<>(dto.getKonsekvenserForYtelsen());

        builder.medKonsekvenserForYtelsen(konsekvenserForYtelsen);
        builder.medErRevurderingMedUendretUtfall(dto.getErRevurderingMedUendretUtfall());
        builder.medSkjæringstidspunkt(Optional.ofNullable(dto.getSkjæringstidspunkt()));
        return builder.build();
    }
}
