package com.example.util;

import com.example.model.Reservation;
import com.example.model.Salle;
import com.example.model.Utilisateur;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.time.LocalDateTime;

public class DataInitializer {

    private final EntityManager em;

    public DataInitializer(EntityManager em) {
        this.em = em;
    }

    public void initData() {
        EntityTransaction tx = em.getTransaction();
        boolean ownTx = false;
        if (!tx.isActive()) {
            tx.begin();
            ownTx = true;
        }

        Utilisateur user1 = new Utilisateur();
        user1.setNom("Alice Dupont");
        user1.setEmail("alice.dupont@example.com");
        em.persist(user1);

        Salle salle1 = new Salle();
        salle1.setNom("Salle A");
        salle1.setCapacite(20);
        em.persist(salle1);

        Reservation res1 = new Reservation();
        res1.setUtilisateur(user1);
        res1.setSalle(salle1);
        res1.setDateDebut(LocalDateTime.now().plusDays(1));
        res1.setDateFin(LocalDateTime.now().plusDays(1).plusHours(2));
        em.persist(res1);

        if (ownTx) {
            tx.commit();
        }
    }
}
