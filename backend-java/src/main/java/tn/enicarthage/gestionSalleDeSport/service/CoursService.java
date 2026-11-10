package tn.enicarthage.gestionSalleDeSport.service;

import tn.enicarthage.gestionSalleDeSport.dao.CoachDao;
import tn.enicarthage.gestionSalleDeSport.dao.CoursDao;
import tn.enicarthage.gestionSalleDeSport.dao.InscriptionDao;
import tn.enicarthage.gestionSalleDeSport.dao.SalleDao;
import tn.enicarthage.gestionSalleDeSport.dto.CoursRequest;
import tn.enicarthage.gestionSalleDeSport.exception.ConflictException;
import tn.enicarthage.gestionSalleDeSport.exception.ResourceNotFoundException;
import tn.enicarthage.gestionSalleDeSport.model.Coach;
import tn.enicarthage.gestionSalleDeSport.model.Cours;
import tn.enicarthage.gestionSalleDeSport.model.Salle;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

public class CoursService {

    private static final int SEMAINES_MAX = 12;

    private final CoursDao coursDao = new CoursDao();
    private final SalleDao salleDao = new SalleDao();
    private final CoachDao coachDao = new CoachDao();
    private final InscriptionDao inscriptionDao = new InscriptionDao();

    // Tous les cours. Si l'utilisateur connecte est un adherent, on marque ceux ou il est inscrit.
    public List<Cours> getAllCours(int utilisateurId) throws SQLException {
        List<Cours> liste = coursDao.getAllCours();
        Integer adherentId = inscriptionDao.getAdherentId(utilisateurId);
        if (adherentId != null) {
            Set<Integer> mesCours = inscriptionDao.getCoursIdsByAdherent(adherentId);
            for (Cours c : liste) {
                c.setInscrit(mesCours.contains(c.getId()));
            }
        }
        return liste;
    }

    public Cours getCours(int id, int utilisateurId) throws SQLException {
        Cours cours = trouver(id);
        Integer adherentId = inscriptionDao.getAdherentId(utilisateurId);
        if (adherentId != null) {
            cours.setInscrit(inscriptionDao.existeInscription(id, adherentId));
        }
        return cours;
    }

    // Cree le cours ; avec "semaines" = N, le cree aussi les N - 1 semaines suivantes (meme jour, meme heure).
    // Renvoie le premier cours cree.
    public Cours createCours(CoursRequest request) throws SQLException {
        int semaines = request.semaines() == null ? 1 : request.semaines();
        if (semaines < 1 || semaines > SEMAINES_MAX) {
            throw new IllegalArgumentException("Le nombre de semaines doit etre entre 1 et " + SEMAINES_MAX + ".");
        }
        Cours modele = valider(request);

        // On verifie d'abord toutes les dates : soit tout est cree, soit rien
        for (int k = 0; k < semaines; k++) {
            verifierSalleLibre(modele.getSalleId(), modele.getDateCours().plusWeeks(k), modele, 0);
        }
        int premierId = 0;
        for (int k = 0; k < semaines; k++) {
            Cours cours = copie(modele);
            cours.setDateCours(modele.getDateCours().plusWeeks(k));
            int id = coursDao.createCours(cours);
            if (k == 0) premierId = id;
        }
        return trouver(premierId); // relu pour avoir coachNom / salleNom
    }

    public Cours updateCours(int id, CoursRequest request) throws SQLException {
        Cours existant = trouver(id);
        Cours cours = valider(request);
        if (cours.getCapacite() < existant.getInscrits()) {
            throw new IllegalArgumentException("Capacite trop petite : " + existant.getInscrits()
                    + " adherent(s) sont deja inscrits.");
        }
        verifierSalleLibre(cours.getSalleId(), cours.getDateCours(), cours, id);
        cours.setId(id);
        coursDao.updateCours(cours);
        return trouver(id);
    }

    public void deleteCours(int id) throws SQLException {
        // ON DELETE CASCADE supprime aussi les inscriptions a ce cours
        if (!coursDao.deleteCours(id)) {
            throw new ResourceNotFoundException("Cours introuvable : " + id);
        }
    }

    Cours trouver(int id) throws SQLException {
        Cours cours = coursDao.getCours(id);
        if (cours == null) {
            throw new ResourceNotFoundException("Cours introuvable : " + id);
        }
        return cours;
    }

    // Deux cours ne peuvent pas avoir lieu dans la meme salle a la meme date et heure
    private void verifierSalleLibre(int salleId, java.time.LocalDate date, Cours cours, int coursExcluId)
            throws SQLException {
        if (coursDao.existeCreneau(salleId, date, cours.getHeure(), coursExcluId)) {
            throw new ConflictException("La salle est deja occupee le " + date + " a " + cours.getHeure() + ".");
        }
    }

    private Cours copie(Cours c) {
        Cours n = new Cours();
        n.setNomCours(c.getNomCours());
        n.setDateCours(c.getDateCours());
        n.setHeure(c.getHeure());
        n.setCapacite(c.getCapacite());
        n.setCoachId(c.getCoachId());
        n.setSalleId(c.getSalleId());
        return n;
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
