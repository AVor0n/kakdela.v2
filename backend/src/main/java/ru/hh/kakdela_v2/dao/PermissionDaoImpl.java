package ru.hh.kakdela_v2.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import ru.hh.kakdela_v2.model.Permission;
import ru.hh.kakdela_v2.model.Permission.PermissionId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PermissionDaoImpl implements PermissionDao {

    private final SessionFactory sessionFactory;

    public PermissionDaoImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    private Session session() {
        return sessionFactory.getCurrentSession();
    }

    @Override
    public Optional<Permission> findById(PermissionId id) {
        return Optional.ofNullable(session().find(Permission.class, id));
    }

    @Override
    public Optional<Permission> findBySurveyIdAndAccountId(UUID surveyId, UUID accountId) {
        return session()
                .createQuery(
                    "SELECT p FROM Permission p WHERE p.id.surveyId = :surveyId AND p.id.accountId = :accountId",
                    Permission.class
                )
                .setParameter("surveyId", surveyId)
                .setParameter("accountId", accountId)
                .uniqueResultOptional();
    }

    @Override
    public List<Permission> findAllBySurveyId(UUID surveyId) {
        return session()
                .createQuery("SELECT p FROM Permission p WHERE p.id.surveyId = :surveyId", Permission.class)
                .setParameter("surveyId", surveyId)
                .getResultList();
    }

    @Override
    public List<Permission> findAllByAccountId(UUID accountId) {
        return session()
                .createQuery("SELECT p FROM Permission p WHERE p.id.accountId = :accountId", Permission.class)
                .setParameter("accountId", accountId)
                .getResultList();
    }

    @Override
    public List<Permission> findAll() {
        return session()
                .createQuery("SELECT p FROM Permission p", Permission.class)
                .getResultList();
    }

    @Override
    public void save(Permission permission) {
        session().persist(permission);
    }

    @Override
    public void update(Permission permission) {
        session().merge(permission);
    }

    @Override
    public void delete(PermissionId id) {
        Permission permission = session().find(Permission.class, id);
        if (permission != null) {
            session().remove(permission);
        }
    }

    @Override
    public void deleteBySurveyIdAndAccountId(UUID surveyId, UUID accountId) {
        session()
                .createMutationQuery("DELETE FROM Permission p WHERE p.id.surveyId = :surveyId AND p.id.accountId = :accountId")
                .setParameter("surveyId", surveyId)
                .setParameter("accountId", accountId)
                .executeUpdate();
    }

    @Override
    public boolean existsBySurveyIdAndAccountId(UUID surveyId, UUID accountId) {
        Long count = session()
                .createQuery(
                    "SELECT COUNT(p) FROM Permission p WHERE p.id.surveyId = :surveyId AND p.id.accountId = :accountId",
                    Long.class
                )
                .setParameter("surveyId", surveyId)
                .setParameter("accountId", accountId)
                .uniqueResult();
        return count != null && count > 0;
    }
}