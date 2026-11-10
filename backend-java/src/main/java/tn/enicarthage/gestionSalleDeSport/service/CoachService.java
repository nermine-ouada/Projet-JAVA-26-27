package tn.enicarthage.gestionSalleDeSport.service;

import tn.enicarthage.gestionSalleDeSport.dao.CoachDao;
import tn.enicarthage.gestionSalleDeSport.model.Coach;

import java.sql.SQLException;
import java.util.List;

public class CoachService {

    private final CoachDao coachDao = new CoachDao();

    public List<Coach> getCoachs() throws SQLException {
        return coachDao.getCoachs();
    }
}
