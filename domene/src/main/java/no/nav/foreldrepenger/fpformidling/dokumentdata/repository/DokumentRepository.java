package no.nav.foreldrepenger.fpformidling.dokumentdata.repository;

import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import no.nav.foreldrepenger.fpformidling.dokumentdata.DokumentData;

@ApplicationScoped
public class DokumentRepository {

    private EntityManager entityManager;

    public DokumentRepository() {
    }

    @Inject
    public DokumentRepository(EntityManager entityManager) {
        Objects.requireNonNull(entityManager, "entityManager");
        this.entityManager = entityManager;
    }

    public void lagre(DokumentData dokumentData) {
        entityManager.persist(dokumentData);
        dokumentData.getDokumentFelles().forEach(entityManager::persist);
        entityManager.flush();
    }
}
