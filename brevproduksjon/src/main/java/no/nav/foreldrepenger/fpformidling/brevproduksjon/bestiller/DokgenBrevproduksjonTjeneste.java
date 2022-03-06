package no.nav.foreldrepenger.fpformidling.brevproduksjon.bestiller;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.fpformidling.behandling.Behandling;
import no.nav.foreldrepenger.fpformidling.brevproduksjon.mapper.felles.DokumentdataMapper;
import no.nav.foreldrepenger.fpformidling.brevproduksjon.mapper.felles.DokumentdataMapperProvider;
import no.nav.foreldrepenger.fpformidling.brevproduksjon.task.BrevTaskProperties;
import no.nav.foreldrepenger.fpformidling.brevproduksjon.task.DistribuerBrevTask;
import no.nav.foreldrepenger.fpformidling.brevproduksjon.task.FerdigstillForsendelseTask;
import no.nav.foreldrepenger.fpformidling.brevproduksjon.task.TilknyttVedleggTask;
import no.nav.foreldrepenger.fpformidling.brevproduksjon.tjenester.DomeneobjektProvider;
import no.nav.foreldrepenger.fpformidling.dokumentdata.BestillingType;
import no.nav.foreldrepenger.fpformidling.dokumentdata.DokumentData;
import no.nav.foreldrepenger.fpformidling.dokumentdata.DokumentFelles;
import no.nav.foreldrepenger.fpformidling.dokumentdata.repository.DokumentRepository;
import no.nav.foreldrepenger.fpformidling.hendelser.DokumentHendelse;
import no.nav.foreldrepenger.fpformidling.historikk.DokumentHistorikkinnslag;
import no.nav.foreldrepenger.fpformidling.historikk.HistorikkAktør;
import no.nav.foreldrepenger.fpformidling.historikk.HistorikkRepository;
import no.nav.foreldrepenger.fpformidling.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.fpformidling.integrasjon.dokgen.Dokgen;
import no.nav.foreldrepenger.fpformidling.integrasjon.dokgen.dto.felles.Dokumentdata;
import no.nav.foreldrepenger.fpformidling.integrasjon.journal.OpprettJournalpostTjeneste;
import no.nav.foreldrepenger.fpformidling.integrasjon.journal.dto.OpprettJournalpostResponse;
import no.nav.foreldrepenger.fpformidling.kafkatjenester.historikk.task.PubliserHistorikkTask;
import no.nav.foreldrepenger.fpformidling.kodeverk.kodeverdi.DokumentMalType;
import no.nav.foreldrepenger.fpformidling.typer.JournalpostId;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskGruppe;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;

@ApplicationScoped
public class DokgenBrevproduksjonTjeneste {
    private static final Logger LOGGER = LoggerFactory.getLogger(DokgenBrevproduksjonTjeneste.class);
    private static final Logger SECURE_LOGGER = LoggerFactory.getLogger("secureLogger");

    private DokumentFellesDataMapper dokumentFellesDataMapper;
    private DomeneobjektProvider domeneobjektProvider;
    private DokumentRepository dokumentRepository;
    private Dokgen dokgenRestKlient;
    private OpprettJournalpostTjeneste opprettJournalpostTjeneste;
    private DokumentdataMapperProvider dokumentdataMapperProvider;
    private ProsessTaskTjeneste taskTjeneste;
    private HistorikkRepository historikkRepository;

    DokgenBrevproduksjonTjeneste() {
        // CDI
    }

    @Inject
    public DokgenBrevproduksjonTjeneste(DokumentFellesDataMapper dokumentFellesDataMapper, DomeneobjektProvider domeneobjektProvider, DokumentRepository dokumentRepository,
            /* @Jersey */Dokgen dokgenRestKlient, OpprettJournalpostTjeneste opprettJournalpostTjeneste, DokumentdataMapperProvider dokumentdataMapperProvider, ProsessTaskTjeneste taskTjeneste, HistorikkRepository historikkRepository) {
        this.dokumentFellesDataMapper = dokumentFellesDataMapper;
        this.domeneobjektProvider = domeneobjektProvider;
        this.dokumentRepository = dokumentRepository;
        this.dokgenRestKlient = dokgenRestKlient;
        this.opprettJournalpostTjeneste = opprettJournalpostTjeneste;
        this.dokumentdataMapperProvider = dokumentdataMapperProvider;
        this.taskTjeneste = taskTjeneste;
        this.historikkRepository = historikkRepository;
    }

    public byte[] forhandsvisBrev(DokumentHendelse dokumentHendelse, Behandling behandling, DokumentMalType dokumentMal) {
        DokumentData dokumentData = lagreDokumentDataFor(behandling, dokumentMal, BestillingType.UTKAST);

        DokumentFelles førsteDokumentFelles = dokumentData.getFørsteDokumentFelles();

        DokumentdataMapper dokumentdataMapper = dokumentdataMapperProvider.getDokumentdataMapper(dokumentMal);
        Dokumentdata dokumentdata = dokumentdataMapper.mapTilDokumentdata(førsteDokumentFelles, dokumentHendelse, behandling, true);
        førsteDokumentFelles.setBrevData(DefaultJsonMapper.toJson(dokumentdata));

        byte[] brev;
        try {
            brev = dokgenRestKlient.genererPdf(dokumentdataMapper.getTemplateNavn(), behandling.getSpråkkode(), dokumentdata);
        } catch (Exception e) {
            dokumentdata.getFelles().anonymiser();
            SECURE_LOGGER.warn("Klarte ikke å generere brev av følgende brevdata: {}", DefaultJsonMapper.toJson(dokumentdata));

            throw new TekniskException("FPFORMIDLING-221006", String.format("Klarte ikke hente forhåndvise mal %s for behandling %s.", dokumentMal.getKode(), behandling.getUuid().toString()), e);
        }
        LOGGER.info("Dokument av type {} i behandling id {} er forhåndsvist", dokumentMal.getKode(), behandling.getUuid().toString());
        return brev;
    }

    public void bestillBrev(DokumentHendelse dokumentHendelse, Behandling behandling, DokumentMalType dokumentMal) {
        DokumentData dokumentData = lagreDokumentDataFor(behandling, dokumentMal, BestillingType.BESTILL);
        boolean innsynMedVedlegg = erInnsynMedVedlegg(behandling, dokumentMal);

        for (DokumentFelles dokumentFelles : dokumentData.getDokumentFelles()) {
            DokumentdataMapper dokumentdataMapper = dokumentdataMapperProvider.getDokumentdataMapper(dokumentMal);
            Dokumentdata dokumentdata = dokumentdataMapper.mapTilDokumentdata(dokumentFelles, dokumentHendelse, behandling, false);
            dokumentFelles.setBrevData(DefaultJsonMapper.toJson(dokumentdata));

            byte[] brev;
            try {
                brev = dokgenRestKlient.genererPdf(dokumentdataMapper.getTemplateNavn(), behandling.getSpråkkode(), dokumentdata);
            } catch (Exception e) {
                dokumentdata.getFelles().anonymiser();
                SECURE_LOGGER.warn("Klarte ikke å generere brev av følgende brevdata: {}", DefaultJsonMapper.toJson(dokumentdata));
                throw new TekniskException("FPFORMIDLING-221045", String.format("Klarte ikke å produsere mal %s for behandling %s.", dokumentMal.getKode(), behandling.getUuid().toString()), e);
            }

            OpprettJournalpostResponse response = opprettJournalpostTjeneste.journalførUtsendelse(brev, dokumentMal, dokumentFelles, dokumentHendelse, behandling.getFagsakBackend().getSaksnummer(), !innsynMedVedlegg, behandling.getBehandlingsresultat() != null ? behandling.getBehandlingsresultat().getOverskrift() : null);

            JournalpostId journalpostId = new JournalpostId(response.getJournalpostId());
            if (innsynMedVedlegg) {
                leggTilVedleggOgFerdigstillForsendelse(dokumentHendelse.getBehandlingUuid(), journalpostId);
            }

            distribuerBrevOgLagHistorikk(dokumentHendelse, dokumentMal, response, journalpostId, innsynMedVedlegg, dokumentFelles.getSaksnummer().getVerdi());
        }
    }

    private DokumentData lagreDokumentDataFor(Behandling behandling, DokumentMalType dokumentMal, BestillingType bestillingType) {
        DokumentData dokumentData = lagDokumentData(behandling, dokumentMal, bestillingType);
        dokumentFellesDataMapper.opprettDokumentDataForBehandling(behandling, dokumentData);
        dokumentRepository.lagre(dokumentData);
        return dokumentData;
    }


    private DokumentData lagDokumentData(Behandling behandling, DokumentMalType dokumentMalType, BestillingType bestillingType) {
        return DokumentData.builder()
                .medDokumentMalType(dokumentMalType)
                .medBehandlingUuid(behandling.getUuid())
                .medBestiltTid(LocalDateTime.now())
                .medBestillingType(bestillingType.name())
                .build();
    }

    private void distribuerBrevOgLagHistorikk(DokumentHendelse dokumentHendelse, DokumentMalType dokumentMal, OpprettJournalpostResponse response, JournalpostId journalpostId, boolean innsynMedVedlegg, String saksnummer) {
        ProsessTaskGruppe taskGruppe = new ProsessTaskGruppe();
        taskGruppe.addNesteSekvensiell(
                opprettDistribuerBrevTask(journalpostId,
                    innsynMedVedlegg,
                    dokumentHendelse.getBehandlingUuid(),
                    saksnummer,
                    dokumentHendelse.getBestillingUuid()));
        DokumentHistorikkinnslag historikkinnslag = lagHistorikkinnslag(dokumentHendelse, response, dokumentMal);
        taskGruppe.addNesteSekvensiell(opprettPubliserHistorikkTask(historikkinnslag));
        taskTjeneste.lagre(taskGruppe);
    }

    private void leggTilVedleggOgFerdigstillForsendelse(UUID behandlingUid, JournalpostId journalpostId) {
        ProsessTaskGruppe taskGruppe = new ProsessTaskGruppe();
        taskGruppe.addNesteSekvensiell(opprettTilknyttVedleggTask(behandlingUid, journalpostId));
        taskGruppe.addNesteSekvensiell(opprettFerdigstillForsendelseTask(journalpostId));
        taskTjeneste.lagre(taskGruppe);
    }

    private ProsessTaskData opprettTilknyttVedleggTask(UUID behandlingUuId, JournalpostId journalpostId) {
        ProsessTaskData prosessTaskData = ProsessTaskData.forProsessTask(TilknyttVedleggTask.class);
        prosessTaskData.setProperty(BrevTaskProperties.JOURNALPOST_ID, journalpostId.getVerdi());
        prosessTaskData.setProperty(BrevTaskProperties.BEHANDLING_UUID, (String.valueOf(behandlingUuId)));
        return prosessTaskData;
    }

    private ProsessTaskData opprettFerdigstillForsendelseTask(JournalpostId journalpostId) {
        ProsessTaskData prosessTaskData = ProsessTaskData.forProsessTask(FerdigstillForsendelseTask.class);
        prosessTaskData.setProperty(BrevTaskProperties.JOURNALPOST_ID, journalpostId.getVerdi());
        return prosessTaskData;
    }

    private ProsessTaskData opprettDistribuerBrevTask(JournalpostId journalpostId, boolean innsynMedVedlegg, UUID behandlingUuId, String saksnummer, UUID bestillingUuid) {
        ProsessTaskData prosessTaskData = ProsessTaskData.forProsessTask(DistribuerBrevTask.class);
        prosessTaskData.setProperty(BrevTaskProperties.JOURNALPOST_ID, journalpostId.getVerdi());
        prosessTaskData.setProperty(BrevTaskProperties.BEHANDLING_UUID, String.valueOf(behandlingUuId));
        prosessTaskData.setProperty(BrevTaskProperties.SAKSNUMMER, saksnummer);
        prosessTaskData.setProperty(BrevTaskProperties.BESTILLING_UUID, String.valueOf(bestillingUuid));
        // må vente til vedlegg er knyttet og journalpost er ferdigstilt
        if (innsynMedVedlegg) {
            prosessTaskData.setNesteKjøringEtter(LocalDateTime.now().plusMinutes(1));
        }
        return prosessTaskData;
    }

    private ProsessTaskData opprettPubliserHistorikkTask(DokumentHistorikkinnslag historikkinnslag) {
        ProsessTaskData prosessTaskData = ProsessTaskData.forProsessTask(PubliserHistorikkTask.class);
        prosessTaskData.setProperty(PubliserHistorikkTask.HISTORIKK_ID, String.valueOf(historikkinnslag.getId()));
        return prosessTaskData;
    }

    private boolean erInnsynMedVedlegg(Behandling behandling, DokumentMalType dokumentMal) {
        if (!DokumentMalType.INNSYN_SVAR.equals(dokumentMal)) {
            return false;
        }
        return !domeneobjektProvider.hentInnsyn(behandling).getInnsynDokumenter().isEmpty();
    }

    private DokumentHistorikkinnslag lagHistorikkinnslag(DokumentHendelse dokumentHendelse,
            OpprettJournalpostResponse response,
            DokumentMalType dokumentMal) {
        DokumentHistorikkinnslag historikkinnslag = DokumentHistorikkinnslag.builder()
                .medBehandlingUuid(dokumentHendelse.getBehandlingUuid())
                .medHistorikkUuid(dokumentHendelse.getBestillingUuid())
                .medHendelseId(dokumentHendelse.getId())
                .medJournalpostId(new JournalpostId(response.getJournalpostId()))
                .medDokumentId(response.getDokumenter().get(0).getDokumentInfoId())
                .medHistorikkAktør(
                        dokumentHendelse.getHistorikkAktør() != null ? dokumentHendelse.getHistorikkAktør() : HistorikkAktør.VEDTAKSLØSNINGEN)
                .medDokumentMalType(dokumentMal)
                .medHistorikkinnslagType(HistorikkinnslagType.BREV_SENT)
                .build();

        historikkRepository.lagre(historikkinnslag);
        LOGGER.info("Opprettet historikkinnslag for bestilt brev: {}", historikkinnslag.toString());
        return historikkinnslag;
    }
}
