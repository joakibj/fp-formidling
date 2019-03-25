package no.nav.foreldrepenger.melding.hendelser;

import java.util.List;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import no.nav.foreldrepenger.melding.eventmottak.EventmottakFeillogg;
import no.nav.vedtak.felles.jpa.HibernateVerktøy;
import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class HendelseRepositoryImpl implements HendelseRepository {

    private EntityManager entityManager;

    public HendelseRepositoryImpl() {
        //CDI
    }

    @Inject
    public HendelseRepositoryImpl(@VLPersistenceUnit EntityManager entityManager) {
        Objects.requireNonNull(entityManager, "entityManager"); //$NON-NLS-1$
        this.entityManager = entityManager;
    }

    @Override
    public void lagre(DokumentHendelse dokumentHendelse) {
        lagreOgFlush(dokumentHendelse);
    }

    @Override
    public void lagre(EventmottakFeillogg eventmottakFeillogg) {
        lagreOgFlush(eventmottakFeillogg);
    }

    @Override
    public DokumentHendelse hentDokumentHendelseMedId(long hendelseId) {
        TypedQuery<DokumentHendelse> query = entityManager.createQuery("from DokumentHendelse where id=:hendelseId", DokumentHendelse.class);
        query.setParameter("hendelseId", hendelseId);
        return HibernateVerktøy.hentEksaktResultat(query);
    }

    @Override
    public List<DokumentHendelse> hentDokumentHendelserForBehandling(long behandlingId) {
        TypedQuery<DokumentHendelse> query = entityManager.createQuery("from DokumentHendelse where behandlingId=:behandlingId", DokumentHendelse.class);
        query.setParameter("behandlingId", behandlingId);
        return query.getResultList();
    }

    private void lagreOgFlush(Object objektTilLagring) {
        entityManager.persist(objektTilLagring);
        entityManager.flush();
    }

}