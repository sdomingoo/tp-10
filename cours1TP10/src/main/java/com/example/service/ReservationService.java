package com.example.service;

import com.example.model.Reservation;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.OptimisticLockException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

public class ReservationService {

    private static final Logger LOGGER = Logger.getLogger(ReservationService.class.getName());
    private final EntityManager em;

    public ReservationService(EntityManager em) {
        this.em = em;
    }

    public void createReservation(Reservation reservation) {
        EntityTransaction tx = em.getTransaction();
        boolean ownTx = false;
        if (!tx.isActive()) {
            tx.begin();
            ownTx = true;
        }

        try {
            em.persist(reservation);
            if (ownTx) {
                tx.commit();
            }
            if (reservation.getUtilisateur() != null) {
                LOGGER.info("Création de la réservation réussie pour utilisateur : " + reservation.getUtilisateur().getEmail());
            }
        } catch (Exception e) {
            if (ownTx && tx.isActive()) {
                tx.rollback();
            }
            LOGGER.severe("Erreur lors de la création de la réservation : " + e.getMessage());
            throw e;
        }
    }

    public List<Reservation> findReservationsBySalleAndDateJPQL(String nomSalle, LocalDateTime date) {
        String jpql = "SELECT r FROM Reservation r WHERE r.salle.nom = :nomSalle AND r.dateDebut > :date";
        return em.createQuery(jpql, Reservation.class)
                .setParameter("nomSalle", nomSalle)
                .setParameter("date", date)
                .setHint("org.hibernate.cacheable", true)
                .getResultList();
    }

    public List<Reservation> findReservationsBySalleAndDateCriteria(String nomSalle, LocalDateTime date) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Reservation> cq = cb.createQuery(Reservation.class);
        Root<Reservation> root = cq.from(Reservation.class);

        Predicate predicate = cb.and(
                cb.equal(root.get("salle").get("nom"), nomSalle),
                cb.greaterThan(root.get("dateDebut"), date)
        );

        cq.select(root).where(predicate);
        return em.createQuery(cq)
                .setHint("org.hibernate.cacheable", true)
                .getResultList();
    }

    public void testOptimisticLocking(Long reservationId) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Reservation res = em.find(Reservation.class, reservationId);
            if (res != null) {
                res.setDateFin(res.getDateFin().plusHours(1));
            }
            tx.commit();
        } catch (OptimisticLockException ole) {
            if (tx.isActive()) {
                tx.rollback();
            }
            System.out.println("Conflit détecté, veuillez réessayer.");
        }
    }
}
