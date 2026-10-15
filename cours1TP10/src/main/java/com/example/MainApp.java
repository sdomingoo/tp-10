package com.example;

import com.example.model.Reservation;
import com.example.service.ReservationService;
import com.example.util.DataInitializer;
import com.example.util.MigrationExecutor;
import com.example.util.PerformanceReport;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.time.LocalDateTime;
import java.util.List;

public class MainApp {

    public static void main(String[] args) throws Exception {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("ReservationPU");
        EntityManager em = emf.createEntityManager();

        MigrationExecutor migrationExecutor = new MigrationExecutor(
            "jdbc:mysql://localhost:3306/reservation",
            "root",
            "password"
        );
        migrationExecutor.executeMigration("db/migration/v2_0_add_description.sql");

        DataInitializer initializer = new DataInitializer(em);
        initializer.initData();

        ReservationService reservationService = new ReservationService(em);

        List<Reservation> jpqlResults = reservationService.findReservationsBySalleAndDateJPQL("Salle A", LocalDateTime.now());
        System.out.println("Reservations trouvees (JPQL) : " + jpqlResults.size());

        List<Reservation> criteriaResults = reservationService.findReservationsBySalleAndDateCriteria("Salle A", LocalDateTime.now());
        System.out.println("Reservations trouvees (Criteria) : " + criteriaResults.size());

        reservationService.testOptimisticLocking(1L);

        PerformanceReport report = new PerformanceReport(em);
        report.printReport();

        em.close();
        emf.close();

        try {
            com.mysql.cj.jdbc.AbandonedConnectionCleanupThread.checkedShutdown();
        } catch (Throwable ignored) {
        }
    }
}
