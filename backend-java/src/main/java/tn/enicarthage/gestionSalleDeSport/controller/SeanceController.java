package tn.enicarthage.gestionSalleDeSport.controller;

import tn.enicarthage.gestionSalleDeSport.dto.SeanceRequest;
import tn.enicarthage.gestionSalleDeSport.dto.UtilisateurResponse;
import tn.enicarthage.gestionSalleDeSport.model.SeancePrivee;
import tn.enicarthage.gestionSalleDeSport.service.SeanceService;

import java.sql.SQLException;
import java.util.List;

public class SeanceController {

    private final SeanceService seanceService = new SeanceService();

    public List<SeancePrivee> getSeances(UtilisateurResponse connecte) throws SQLException {
        return seanceService.getSeances(connecte);
    }

    public SeancePrivee getSeance(int id, UtilisateurResponse connecte) throws SQLException {
        return seanceService.getSeance(id, connecte);
    }

    public SeancePrivee createSeance(SeanceRequest request, UtilisateurResponse connecte) throws SQLException {
        return seanceService.createSeance(request, connecte);
    }

    public SeancePrivee changerStatut(int id, String statut, UtilisateurResponse connecte) throws SQLException {
        return seanceService.changerStatut(id, statut, connecte);
    }

    public void deleteSeance(int id) throws SQLException {
        seanceService.deleteSeance(id);
    }
}
