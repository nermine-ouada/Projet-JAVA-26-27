package tn.enicarthage.gestionSalleDeSport.controller;

import tn.enicarthage.gestionSalleDeSport.dto.CoursRequest;
import tn.enicarthage.gestionSalleDeSport.model.Cours;
import tn.enicarthage.gestionSalleDeSport.service.CoursService;

import java.sql.SQLException;
import java.util.List;

public class CoursController {

    private final CoursService coursService = new CoursService();

    public List<Cours> getAllCours() throws SQLException {
        return coursService.getAllCours();
    }

    public Cours getCours(int id) throws SQLException {
        return coursService.getCours(id);
    }

    public Cours createCours(CoursRequest request) throws SQLException {
        return coursService.createCours(request);
    }

    public Cours updateCours(int id, CoursRequest request) throws SQLException {
        return coursService.updateCours(id, request);
    }

    public void deleteCours(int id) throws SQLException {
        coursService.deleteCours(id);
    }
}
