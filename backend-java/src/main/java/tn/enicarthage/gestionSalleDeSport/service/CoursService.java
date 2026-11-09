package tn.enicarthage.gestionSalleDeSport.service;

import tn.enicarthage.gestionSalleDeSport.dao.CoachDao;
import tn.enicarthage.gestionSalleDeSport.dao.CoursDao;
import tn.enicarthage.gestionSalleDeSport.dao.SalleDao;
import tn.enicarthage.gestionSalleDeSport.dto.CoursRequest;
import tn.enicarthage.gestionSalleDeSport.exception.ResourceNotFoundException;
import tn.enicarthage.gestionSalleDeSport.model.Coach;
import tn.enicarthage.gestionSalleDeSport.model.Cours;
import tn.enicarthage.gestionSalleDeSport.model.Salle;

import java.sql.SQLException;
import java.util.List;

public class CoursService {

    private final CoursDao coursDao = new CoursDao();
    private final SalleDao salleDao = new SalleDao();
    private final CoachDao coachDao = new CoachDao();

    public List<Cours> getAllCours() throws SQLException {
        return coursDao.getAllCours();
    }

    public Cours getCours(int id) throws SQLException {
        Cours cours = coursDao.getCours(id);
        if (cours == null) {
            throw new ResourceNotFoundException("Cours introuvable : " + id);
        }
        return cours;
    }

    public Cours createCours(CoursRequest request) throws SQLException {
        Cours cours = valider(request);
        int id = coursDao.createCours(cours);
        return coursDao.getCours(id); // relu pour avoir coachNom / salleNom
    }

    public Cours updateCours(int id, CoursRequest request) throws SQLException {
        Cours cours = valider(request);
        cours.setId(id);
        if (!coursDao.updateCours(cours)) {
            throw new ResourceNotFoundException("Cours introuvable : " + id);
        }
        return coursDao.getCours(id);
    }

    public void deleteCours(int id) throws SQLException {
        // ON DELETE CASCADE supprime aussi les inscriptions a ce cours
        if (!coursDao.deleteCours(id)) {
            throw new ResourceNotFoundException("Cours introuvable : " + id);
        }
    }

    // Verifie les champs et les regles metier, puis construit le model
    private Cours valider(CoursRequest r) throws SQLException {
        if (r.nomCours() == null || r.nomCours().isBlank()) {
            throw new IllegalArgumentException("Le nom du cours est obligatoire.");
        }
        if (r.dateCours() == null || r.heure() == null) {
            throw new IllegalArgumentException("La date et l'heure sont obligatoires.");
        }
        if (r.capacite() == null || r.capacite() <= 0) {
            throw new IllegalArgumentException("La capacite doit etre un nombre positif.");
        }
        Coach coach = r.coachId() == null ? null : coachDao.getCoach(r.coachId());
        if (coach == null) {
            throw new IllegalArgumentException("Coach invalide.");
        }
        Salle salle = r.salleId() == null ? null : salleDao.getSalle(r.salleId());
        if (salle == null) {
            throw new IllegalArgumentException("Salle invalide.");
        }
        if (r.capacite() > salle.getCapacite()) {
            throw new IllegalArgumentException("La capacite du cours depasse celle de la salle ("
                    + salle.getCapacite() + " places).");
        }

        Cours cours = new Cours();
        cours.setNomCours(r.nomCours().trim());
        cours.setDateCours(r.dateCours());
        cours.setHeure(r.heure());
        cours.setCapacite(r.capacite());
        cours.setCoachId(r.coachId());
        cours.setSalleId(r.salleId());
        return cours;
    }
}
