package tn.enicarthage.gestionSalleDeSport.service;

import tn.enicarthage.gestionSalleDeSport.dao.AbonnementDao;
import tn.enicarthage.gestionSalleDeSport.dao.AdherentDao;
import tn.enicarthage.gestionSalleDeSport.dto.AbonnementRequest;
import tn.enicarthage.gestionSalleDeSport.exception.ResourceNotFoundException;
import tn.enicarthage.gestionSalleDeSport.model.Abonnement;
import tn.enicarthage.gestionSalleDeSport.model.Adherent;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public class AbonnementService {

    private static final Set<String> TYPES = Set.of("Mensuel", "Trimestriel", "Annuel");

    private final AbonnementDao abonnementDao = new AbonnementDao();
    private final AdherentDao adherentDao = new AdherentDao();

    public List<Abonnement> getAbonnements() throws SQLException {
        return avecStatut(abonnementDao.getAbonnements());
    }

    // Abonnements de l'utilisateur connecte (role ADHERENT)
    public List<Abonnement> getMesAbonnements(int utilisateurId) throws SQLException {
        Adherent adherent = adherentDao.getAdherentByUtilisateur(utilisateurId);
        if (adherent == null) {
            return List.of();
        }
        return avecStatut(abonnementDao.getAbonnementsByAdherent(adherent.getId()));
    }

    public Abonnement getAbonnement(int id) throws SQLException {
        Abonnement abonnement = abonnementDao.getAbonnement(id);
        if (abonnement == null) {
            throw new ResourceNotFoundException("Abonnement introuvable : " + id);
        }
        abonnement.setStatut(calculerStatut(abonnement));
        return abonnement;
    }

    public Abonnement createAbonnement(AbonnementRequest request) throws SQLException {
        Abonnement abonnement = valider(request);
        int id = abonnementDao.createAbonnement(abonnement);
        return getAbonnement(id);
    }

    public Abonnement updateAbonnement(int id, AbonnementRequest request) throws SQLException {
        Abonnement abonnement = valider(request);
        abonnement.setId(id);
        if (!abonnementDao.updateAbonnement(abonnement)) {
            throw new ResourceNotFoundException("Abonnement introuvable : " + id);
        }
        return getAbonnement(id);
    }

    public void deleteAbonnement(int id) throws SQLException {
        if (!abonnementDao.deleteAbonnement(id)) {
            throw new ResourceNotFoundException("Abonnement introuvable : " + id);
        }
    }

    public List<Adherent> getAdherents() throws SQLException {
        return adherentDao.getAdherents();
    }

    // ACTIF si aujourd'hui est entre le debut et la fin, EXPIRE si la fin est passee, sinon A_VENIR
    private String calculerStatut(Abonnement ab) {
        LocalDate aujourdhui = LocalDate.now();
        if (ab.getDateFin().isBefore(aujourdhui)) {
            return "EXPIRE";
        }
        if (ab.getDateDebut().isAfter(aujourdhui)) {
            return "A_VENIR";
        }
        return "ACTIF";
    }

    private List<Abonnement> avecStatut(List<Abonnement> liste) {
        for (Abonnement ab : liste) {
            ab.setStatut(calculerStatut(ab));
        }
        return liste;
    }

    private Abonnement valider(AbonnementRequest r) throws SQLException {
        if (r.type() == null || !TYPES.contains(r.type())) {
            throw new IllegalArgumentException("Type invalide (Mensuel, Trimestriel ou Annuel).");
        }
        if (r.dateDebut() == null || r.dateFin() == null) {
            throw new IllegalArgumentException("Les dates de debut et de fin sont obligatoires.");
        }
        if (!r.dateFin().isAfter(r.dateDebut())) {
            throw new IllegalArgumentException("La date de fin doit etre apres la date de debut.");
        }
        if (r.prix() == null || r.prix().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le prix doit etre un nombre positif.");
        }
        if (r.adherentId() == null || adherentDao.getAdherent(r.adherentId()) == null) {
            throw new IllegalArgumentException("Adherent invalide.");
        }

        Abonnement ab = new Abonnement();
        ab.setType(r.type());
        ab.setDateDebut(r.dateDebut());
        ab.setDateFin(r.dateFin());
        ab.setPrix(r.prix());
        ab.setAdherentId(r.adherentId());
        return ab;
    }
}
