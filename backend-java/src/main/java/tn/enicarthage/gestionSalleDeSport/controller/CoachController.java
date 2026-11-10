package tn.enicarthage.gestionSalleDeSport.controller;

import tn.enicarthage.gestionSalleDeSport.model.Coach;
import tn.enicarthage.gestionSalleDeSport.service.CoachService;

import java.sql.SQLException;
import java.util.List;

public class CoachController {

    private final CoachService coachService = new CoachService();

    public List<Coach> getCoachs() throws SQLException {
        return coachService.getCoachs();
    }
}
