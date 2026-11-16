package tn.enicarthage.gestionSalleDeSport.controller;

import tn.enicarthage.gestionSalleDeSport.dto.AbonnementRequest;
import tn.enicarthage.gestionSalleDeSport.model.Abonnement;
import tn.enicarthage.gestionSalleDeSport.model.Adherent;
import tn.enicarthage.gestionSalleDeSport.service.AbonnementService;

import java.sql.SQLException;
import java.util.List;

public class AbonnementController {

    private final AbonnementService abonnementService = new AbonnementService();

    public List<Abonnement> getAbonnements() throws SQLException {
        return abonnementService.getAbonnements();
    }

    public List<Abonnement> getMesAbonnements(int utilisateurId) throws SQLException {
        return abonnementService.getMesAbonnements(utilisateurId);
    }

    public Abonnement getAbonnement(int id) throws SQLException {
        return abonnementService.getAbonnement(id);
    }

    public Abonnement createAbonnement(AbonnementRequest request) throws SQLException {
        return abonnementService.createAbonnement(request);
    }

    public Abonnement updateAbonnement(int id, AbonnementRequest request) throws SQLException {
        return abonnementService.updateAbonnement(id, request);
    }

    public void deleteAbonnement(int id) throws SQLException {
        abonnementService.deleteAbonnement(id);
    }

    public List<Adherent> getAdherents() throws SQLException {
        return abonnementService.getAdherents();
    }
}
