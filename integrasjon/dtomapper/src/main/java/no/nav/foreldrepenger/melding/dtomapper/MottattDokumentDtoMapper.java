package no.nav.foreldrepenger.melding.dtomapper;

import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.fpsak.dto.behandling.MottattDokumentDto;
import no.nav.foreldrepenger.melding.dokumentdata.DokumentKategori;
import no.nav.foreldrepenger.melding.dokumentdata.DokumentTypeId;
import no.nav.foreldrepenger.melding.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.melding.mottattdokument.MottattDokument;

@ApplicationScoped
public class MottattDokumentDtoMapper {

    private KodeverkRepository kodeverkRepository;

    @Inject
    public MottattDokumentDtoMapper(KodeverkRepository kodeverkRepository) {
        this.kodeverkRepository = kodeverkRepository;
    }

    public MottattDokumentDtoMapper() {
        //CDI
    }

    public List<MottattDokument> mapMottattedokumenterFraDto(List<MottattDokumentDto> dtoListe) {
        return dtoListe.stream().map(this::mapMottattDokumentFraDto).collect(Collectors.toList());
    }

    private MottattDokument mapMottattDokumentFraDto(MottattDokumentDto dto) {
        return new MottattDokument(dto.getMottattDato(), kodeverkRepository.finn(DokumentTypeId.class, dto.getDokumentTypeId().getKode()), kodeverkRepository.finn(DokumentKategori.class, dto.getDokumentKategori().getKode()));
    }

}