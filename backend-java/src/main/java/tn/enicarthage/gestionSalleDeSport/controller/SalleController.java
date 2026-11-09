package tn.enicarthage.gestionSalleDeSport.controller;

import tn.enicarthage.gestionSalleDeSport.dto.SalleRequest;
import tn.enicarthage.gestionSalleDeSport.model.Salle;
import tn.enicarthage.gestionSalleDeSport.service.SalleService;

import java.sql.SQLException;
import java.util.List;

public class SalleController {

    private final SalleService salleService = new SalleService();

    public List<Salle> getSalles() throws SQLException {
        return salleService.getSalles();
    }

    public Salle getSalle(int id) throws SQLException {
        return salleService.getSalle(id);
    }

    public Salle createSalle(SalleRequest request) throws SQLException {
        return salleService.createSalle(request);
    }

    public Salle updateSalle(int id, SalleRequest request) throws SQLException {
        return salleService.updateSalle(id, request);
    }

    public void deleteSalle(int id) throws SQLException {
        salleService.deleteSalle(id);
    }
}
