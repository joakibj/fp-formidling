package no.nav.foreldrepenger.fpformidling.uttak.svp;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;

import no.nav.foreldrepenger.fpformidling.behandling.ÅrsakMedLovReferanse;
import no.nav.foreldrepenger.fpformidling.kodeverk.kodeverdi.Kodeverdi;

@SuppressWarnings("java:S115")
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.ANY)
public enum PeriodeIkkeOppfyltÅrsak implements Kodeverdi, ÅrsakMedLovReferanse {

    INGEN("-", null),

    BARNET_ER_DØD("4072", "14-9"),
    SØKER_ER_DØD("4071", "14-10"),
    OPPHØR_MEDLEMSKAP("4087", "14-2"),
    SVANGERSKAPSVILKÅRET_IKKE_OPPFYLT("4098", "14-5"),
    OPPTJENINGSVILKÅRET_IKKE_OPPFYLT("4099", "14-6"),
    _8304("8304", "14-4"),
    _8305("8305",  "14-4"),
    _8306("8306", "14-4"),
    SØKT_FOR_SENT("8308", "14-4"),
    _8309("8309", "14-4"),
    _8310("8310", "14-4"),
    PERIODE_SAMTIDIG_SOM_FERIE("8311", "14-4"),
    _8313("8313", "14-4"),
    _8314("8314", "14-4"),
            ;

    private static final Map<String, PeriodeIkkeOppfyltÅrsak> KODER = new LinkedHashMap<>();

    public static final String KODEVERK = "SVP_PERIODE_IKKE_OPPFYLT_AARSAK";

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    @JsonValue
    private String kode;

    @JsonIgnore
    private String lovHjemmel;

    PeriodeIkkeOppfyltÅrsak(String kode, String lovHjemmel) {
        this.kode = kode;
        this.lovHjemmel = lovHjemmel;
    }


    @Override
    public String getKodeverk() {
        return KODEVERK;
    }

    @Override
    public String getKode() {
        return kode;
    }

    @Override
    public String getLovHjemmelData() {
        return lovHjemmel;
    }

    public static Set<PeriodeIkkeOppfyltÅrsak> opphørsAvslagÅrsaker() {
        return new HashSet<>(Arrays.asList(
                _8304,
                _8305,
                _8306,
                _8309,
                _8314,
                SØKT_FOR_SENT));
    }

}

