package no.nav.foreldrepenger.fpformidling.behandling;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import no.nav.foreldrepenger.fpformidling.fagsak.FagsakBackend;
import no.nav.foreldrepenger.fpformidling.geografisk.Språkkode;
import no.nav.foreldrepenger.fpformidling.kodeverk.kodeverdi.BehandlingÅrsakType;
import no.nav.foreldrepenger.fpformidling.vilkår.Vilkår;

public class Behandling {
    private Behandlingsresultat behandlingsresultat;
    private List<BehandlingResourceLink> resourceLinker;
    private List<BehandlingResourceLink> formidlingRessurser;

    //Felter brukt i brev
    private UUID uuid;
    private BehandlingType behandlingType;
    private Integer behandlingstidFristUker;
    private LocalDateTime opprettetDato;
    private LocalDateTime avsluttet;
    private List<BehandlingÅrsak> behandlingÅrsaker;
    private String ansvarligSaksbehandler;
    private boolean toTrinnsBehandling;

    private String behandlendeEnhetId;
    private String behandlendeEnhetNavn;
    private String ansvarligBeslutter;
    private FagsakBackend fagsakBackend;
    private BehandlingStatus status;
    private String endretAv;
    private Språkkode språkkode;
    private LocalDate originalVedtaksDato;
    private boolean harAvklartAnnenForelderRett;
    private List<Vilkår> vilkår;
    private UUID originalBehandlingUuid;

    private boolean kreverSammenhengendeUttak;

    private Behandling() {
    }


    public LocalDateTime getAvsluttet() {
        return avsluttet;
    }

    public String getBehandlendeEnhetNavn() {
        return behandlendeEnhetNavn;
    }

    public boolean behandlesAvKlageinstans() {
        return behandlendeEnhetNavn != null && behandlendeEnhetNavn.startsWith("NAV Klageinstans");
    }

    public Behandlingsresultat getBehandlingsresultat() {
        return behandlingsresultat;
    }

    public BehandlingType getBehandlingType() {
        return behandlingType;
    }

    public Integer getBehandlingstidFristUker() {
        return behandlingstidFristUker;
    }

    public LocalDateTime getOpprettetDato() {
        return opprettetDato;
    }

    public List<BehandlingÅrsak> getBehandlingÅrsaker() {
        return behandlingÅrsaker;
    }

    public String getAnsvarligSaksbehandler() {
        return ansvarligSaksbehandler;
    }

    public String getAnsvarligBeslutter() {
        return ansvarligBeslutter;
    }

    public boolean isToTrinnsBehandling() {
        return toTrinnsBehandling;
    }

    public UUID getUuid() {
        return uuid;
    }

    public boolean getHarAvklartAnnenForelderRett() {
        return harAvklartAnnenForelderRett;
    }

    public List<Vilkår> getVilkår() {
        return vilkår;
    }

    public List<BehandlingResourceLink> getResourceLinker() {
        return resourceLinker;
    }

    public Språkkode getSpråkkode() {
        return språkkode;
    }

    public LocalDate getOriginalVedtaksDato() {
        return originalVedtaksDato;
    }

    public boolean harBehandlingÅrsak(BehandlingÅrsakType behandlingÅrsak) {
        return getBehandlingÅrsaker().stream().map(BehandlingÅrsak::getBehandlingÅrsakType).anyMatch(behandlingÅrsak::equals);
    }

    public boolean erFørstegangssøknad() {
        return BehandlingType.FØRSTEGANGSSØKNAD.equals(getBehandlingType());
    }

    public BehandlingStatus getStatus() {
        return status;
    }

    public boolean erRevurdering() {
        return BehandlingType.REVURDERING.equals(getBehandlingType());
    }

    public boolean erKlage() {
        return BehandlingType.KLAGE.equals(getBehandlingType());
    }

    public boolean erAnke() {
        return BehandlingType.ANKE.equals(getBehandlingType());
    }

    public boolean erInnsyn() {
        return BehandlingType.INNSYN.equals(getBehandlingType());
    }

    public FagsakBackend getFagsakBackend() {
        return fagsakBackend;
    }

    public boolean kreverSammenhengendeUttakFraBehandlingen() {
        return kreverSammenhengendeUttak;
    }

    public boolean erSaksbehandlingAvsluttet() {
        if (behandlingsresultat == null) {
            return false;
        }
        return erAvsluttet() || erUnderIverksettelse() || erHenlagt();
    }

    private boolean erHenlagt() {
        return getBehandlingsresultat().isBehandlingHenlagt();
    }

    public boolean erUnderIverksettelse() {
        return Objects.equals(BehandlingStatus.IVERKSETTER_VEDTAK, getStatus());
    }

    public boolean erAvsluttet() {
        return Objects.equals(BehandlingStatus.AVSLUTTET, getStatus());
    }

    public String getEndretAv() {
        return endretAv;
    }

    public String getBehandlendeEnhetId() {
        return behandlendeEnhetId;
    }

    public boolean utenMinsterett() {
        return Optional.ofNullable(getBehandlingsresultat()).map(Behandlingsresultat::utenMinsterett).orElse(true);
    }

    public UUID getOriginalBehandlingUuid() {
        return originalBehandlingUuid;
    }

    public void leggtilFagsakBackend(FagsakBackend fagsak) {
        if (this.fagsakBackend == null) {
            this.fagsakBackend = fagsak;
        }
    }

    public List<BehandlingResourceLink> getFormidlingRessurser() {
        return formidlingRessurser;
    }

    public boolean harFormidlingRessurs(String ressursRel) {
        return formidlingRessurser.stream().anyMatch(lenk -> lenk.getRel().equals(ressursRel));
    }

    public boolean harFagsakBackend() {
        return fagsakBackend != null;
    }

    public boolean erManueltOpprettet() {
        return getBehandlingÅrsaker().stream().map(BehandlingÅrsak::getManueltOpprettet).toList().contains(true);
    }

    public static Behandling.Builder builder() {
        return new Behandling.Builder();
    }

    public static class Builder {
        private Behandling kladd;

        public Builder() {
            this.kladd = new Behandling();
            this.kladd.resourceLinker = new ArrayList<>();
            this.kladd.formidlingRessurser = new ArrayList<>();
            this.kladd.behandlingÅrsaker = new ArrayList<>();
        }

        public Behandling.Builder medBehandlendeEnhetId(String behandlendeEnhetId) {
            this.kladd.behandlendeEnhetId = behandlendeEnhetId;
            return this;
        }

        public Behandling.Builder medBehandlendeEnhetNavn(String behandlendeEnhetNavn) {
            this.kladd.behandlendeEnhetNavn = behandlendeEnhetNavn;
            return this;
        }

        public Behandling.Builder medBehandlingsresultat(Behandlingsresultat behandlingsresultat) {
            this.kladd.behandlingsresultat = behandlingsresultat;
            return this;
        }

        public Behandling.Builder leggTilResourceLink(BehandlingResourceLink resourceLink) {
            this.kladd.resourceLinker.add(resourceLink);
            return this;
        }

        public Behandling.Builder leggTilFormidlingResourceLink(BehandlingResourceLink resourceLink) {
            this.kladd.formidlingRessurser.add(resourceLink);
            return this;
        }

        public Behandling.Builder medUuid(UUID uuid) {
            this.kladd.uuid = uuid;
            return this;
        }

        public Behandling.Builder medBehandlingType(BehandlingType behandlingType) {
            this.kladd.behandlingType = behandlingType;
            return this;
        }

        public Behandling.Builder medBehandlingstidFristUker(Integer behandlingstidFristUker) {
            this.kladd.behandlingstidFristUker = behandlingstidFristUker;
            return this;
        }

        public Behandling.Builder medOpprettetDato(LocalDateTime opprettetDato) {
            this.kladd.opprettetDato = opprettetDato;
            return this;
        }

        public Behandling.Builder medAvsluttet(LocalDateTime avsluttet) {
            this.kladd.avsluttet = avsluttet;
            return this;
        }

        public Behandling.Builder medBehandlingÅrsaker(List<BehandlingÅrsak> behandlingÅrsaker) {
            this.kladd.behandlingÅrsaker.addAll(behandlingÅrsaker);
            return this;
        }

        public Behandling.Builder medAnsvarligSaksbehandler(String ansvarligSaksbehandler) {
            this.kladd.ansvarligSaksbehandler = ansvarligSaksbehandler;
            return this;
        }

        public Behandling.Builder medToTrinnsBehandling(boolean toTrinnsBehandling) {
            this.kladd.toTrinnsBehandling = toTrinnsBehandling;
            return this;
        }

        public Behandling.Builder medAnsvarligBeslutter(String ansvarligBeslutter) {
            this.kladd.ansvarligBeslutter = ansvarligBeslutter;
            return this;
        }

        public Behandling.Builder medStatus(BehandlingStatus status) {
            this.kladd.status = status;
            return this;
        }

        public Behandling.Builder medFagsakBackend(FagsakBackend fagsak) {
            this.kladd.fagsakBackend = fagsak;
            return this;
        }

        public Behandling.Builder medEndretAv(String endretAv) {
            this.kladd.endretAv = endretAv;
            return this;
        }

        public Behandling.Builder medSpråkkode(Språkkode språkkode) {
            this.kladd.språkkode = språkkode;
            return this;
        }

        public Behandling.Builder medOriginalVedtaksDato(LocalDate originalVedtaksDato) {
            this.kladd.originalVedtaksDato = originalVedtaksDato;
            return this;
        }

        public Behandling.Builder medOriginalBehandlingUuid(UUID uuid) {
            this.kladd.originalBehandlingUuid = uuid;
            return this;
        }

        public Behandling.Builder medHarAvklartAnnenForelderRett(boolean harAvklartAnnenForelderRett) {
            this.kladd.harAvklartAnnenForelderRett = harAvklartAnnenForelderRett;
            return this;
        }

        public Behandling.Builder medVilkår(List<Vilkår> vilkår) {
            this.kladd.vilkår = vilkår;
            return this;
        }

        public Behandling.Builder medKreverSammenhengendeUttak(boolean kreverSammenhengendeUttak) {
            this.kladd.kreverSammenhengendeUttak = kreverSammenhengendeUttak;
            return this;
        }

        public Behandling build() {
            return this.kladd;
        }
    }
}
