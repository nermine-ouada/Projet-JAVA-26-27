package tn.enicarthage.gestionSalleDeSport.service;

import tn.enicarthage.gestionSalleDeSport.dao.AdherentDao;
import tn.enicarthage.gestionSalleDeSport.dao.CoachDao;
import tn.enicarthage.gestionSalleDeSport.dao.SeanceDao;
import tn.enicarthage.gestionSalleDeSport.dto.SeanceRequest;
import tn.enicarthage.gestionSalleDeSport.dto.UtilisateurResponse;
import tn.enicarthage.gestionSalleDeSport.exception.ConflictException;
import tn.enicarthage.gestionSalleDeSport.exception.ForbiddenException;
import tn.enicarthage.gestionSalleDeSport.exception.ResourceNotFoundException;
import tn.enicarthage.gestionSalleDeSport.model.Adherent;
import tn.enicarthage.gestionSalleDeSport.model.SeancePrivee;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

// Coaching prive : l'adherent reserve, le coach confirme ou refuse, l'admin gere tout
public class SeanceService {

    private static final Set<Integer> DUREES = Set.of(30, 45, 60, 90);
    private static final Set<String> STATUTS = Set.of("CONFIRMEE", "ANNULEE");

    private final SeanceDao seanceDao = new SeanceDao();
    private final CoachDao coachDao = new CoachDao();
    private final AdherentDao adherentDao = new AdherentDao();

    // Admin : toutes les seances. Coach et adherent : seulement les leurs.
    public List<SeancePrivee> getSeances(UtilisateurResponse connecte) throws SQLException {
        if (connecte.role().equals("ADMIN")) {
            return seanceDao.getSeances();
        }
        return seanceDao.getSeancesByUtilisateur(connecte.id());
    }

    public SeancePrivee getSeance(int id, UtilisateurResponse connecte) throws SQLException {
        SeancePrivee s = trouver(id);
        verifierAcces(s, connecte);
        return s;
    }

    public SeancePrivee createSeance(SeanceRequest r, UtilisateurResponse connecte) throws SQLException {
        int adherentId;
        String statut;
        if (connecte.role().equals("ADHERENT")) {
            // L'adherent reserve pour lui-meme ; le coach devra confirmer
            adherentId = adherentDao.getAdherentByUtilisateur(connecte.id()).getId();
            statut = "EN_ATTENTE";
        } else if (connecte.role().equals("ADMIN")) {
            Adherent adherent = r.adherentId() == null ? null : adherentDao.getAdherent(r.adherentId());
            if (adherent == null) {
                throw new IllegalArgumentException("Adherent invalide.");
            }
            adherentId = adherent.getId();
            statut = "CONFIRMEE"; // reservee par l'accueil : deja validee
        } else {
            throw new ForbiddenException("Un coach ne peut pas reserver de seance privee.");
        }

        if (r.coachId() == null || coachDao.getCoach(r.coachId()) == null) {
            throw new IllegalArgumentException("Coach invalide.");
        }
        if (r.dateSeance() == null || r.heure() == null) {
            throw new IllegalArgumentException("La date et l'heure sont obligatoires.");
        }
        if (LocalDateTime.of(r.dateSeance(), r.heure()).isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Choisissez une date et une heure a venir.");
        }
        int duree = r.dureeMinutes() == null ? 60 : r.dureeMinutes();
        if (!DUREES.contains(duree)) {
            throw new IllegalArgumentException("Duree invalide (30, 45, 60 ou 90 minutes).");
        }
        if (r.objectif() != null && r.objectif().trim().length() > 255) {
            throw new IllegalArgumentException("L'objectif ne doit pas depasser 255 caracteres.");
        }
        if (seanceDao.existeChevauchement(r.coachId(), r.dateSeance(), r.heure(), duree)) {
            throw new ConflictException("Ce coach a deja une seance a ce moment. Choisissez un autre creneau.");
        }

        SeancePrivee s = new SeancePrivee();
        s.setCoachId(r.coachId());
        s.setAdherentId(adherentId);
        s.setDateSeance(r.dateSeance());
        s.setHeure(r.heure());
        s.setDureeMinutes(duree);
        s.setObjectif(r.objectif() == null || r.objectif().isBlank() ? null : r.objectif().trim());
        s.setStatut(statut);
        return trouver(seanceDao.createSeance(s));
    }

    // Coach (ses seances) : confirmer ou annuler. Adherent (les siennes) : annuler. Admin : tout.
    public SeancePrivee changerStatut(int id, String statut, UtilisateurResponse connecte) throws SQLException {
        if (statut == null || !STATUTS.contains(statut)) {
            throw new IllegalArgumentException("Statut invalide (CONFIRMEE ou ANNULEE).");
        }
        SeancePrivee s = trouver(id);
        verifierAcces(s, connecte);
        if (connecte.role().equals("ADHERENT") && !statut.equals("ANNULEE")) {
            throw new ForbiddenException("Seul le coach peut confirmer une seance.");
        }
        if (s.getStatut().equals("ANNULEE")) {
            throw new IllegalArgumentException("Cette seance est deja annulee.");
        }
        if (LocalDateTime.of(s.getDateSeance(), s.getHeure()).isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Cette seance est deja passee.");
        }
        seanceDao.updateStatut(id, statut);
        return trouver(id);
    }

    public void deleteSeance(int id) throws SQLException {
        if (!seanceDao.deleteSeance(id)) {
            throw new ResourceNotFoundException("Seance introuvable : " + id);
        }
    }

    private SeancePrivee trouver(int id) throws SQLException {
        SeancePrivee s = seanceDao.getSeance(id);
        if (s == null) {
            throw new ResourceNotFoundException("Seance introuvable : " + id);
        }
        return s;
    }

    // Une seance privee n'est visible que par son coach, son adherent et l'admin
    private void verifierAcces(SeancePrivee s, UtilisateurResponse connecte) {
        boolean autorise = connecte.role().equals("ADMIN")
                || s.getCoachUtilisateurId() == connecte.id()
                || s.getAdherentUtilisateurId() == connecte.id();
        if (!autorise) {
            throw new ForbiddenException("Cette seance ne vous concerne pas.");
        }
    }
}
