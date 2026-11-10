package tn.enicarthage.gestionSalleDeSport.service;

import tn.enicarthage.gestionSalleDeSport.dao.InscriptionDao;
import tn.enicarthage.gestionSalleDeSport.exception.ConflictException;
import tn.enicarthage.gestionSalleDeSport.exception.ForbiddenException;
import tn.enicarthage.gestionSalleDeSport.exception.ResourceNotFoundException;
import tn.enicarthage.gestionSalleDeSport.model.Cours;
import tn.enicarthage.gestionSalleDeSport.model.Participant;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

// Inscription d'un adherent a un cours collectif
public class InscriptionService {

    private final InscriptionDao inscriptionDao = new InscriptionDao();
    private final CoursService coursService = new CoursService();

    public Cours inscrire(int coursId, int utilisateurId) throws SQLException {
        int adherentId = adherentConnecte(utilisateurId);
        Cours cours = coursService.trouver(coursId); // 404 si le cours n'existe pas
        verifierAVenir(cours);
        if (inscriptionDao.existeInscription(coursId, adherentId)) {
            throw new ConflictException("Vous etes deja inscrit a ce cours.");
        }
        if (cours.getInscrits() >= cours.getCapacite()) {
            throw new ConflictException("Ce cours est complet.");
        }
        inscriptionDao.createInscription(coursId, adherentId);
        return coursService.getCours(coursId, utilisateurId);
    }

    public void desinscrire(int coursId, int utilisateurId) throws SQLException {
        int adherentId = adherentConnecte(utilisateurId);
        Cours cours = coursService.trouver(coursId);
        verifierAVenir(cours);
        if (!inscriptionDao.deleteInscription(coursId, adherentId)) {
            throw new ResourceNotFoundException("Vous n'etes pas inscrit a ce cours.");
        }
    }

    public List<Participant> getParticipants(int coursId) throws SQLException {
        coursService.trouver(coursId);
        return inscriptionDao.getParticipants(coursId);
    }

    private int adherentConnecte(int utilisateurId) throws SQLException {
        Integer adherentId = inscriptionDao.getAdherentId(utilisateurId);
        if (adherentId == null) {
            throw new ForbiddenException("Seuls les adherents peuvent s'inscrire a un cours.");
        }
        return adherentId;
    }

    // On ne s'inscrit pas (et on ne se desinscrit pas) a un cours deja commence
    private void verifierAVenir(Cours cours) {
        if (LocalDateTime.of(cours.getDateCours(), cours.getHeure()).isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Ce cours est deja passe.");
        }
    }
}
