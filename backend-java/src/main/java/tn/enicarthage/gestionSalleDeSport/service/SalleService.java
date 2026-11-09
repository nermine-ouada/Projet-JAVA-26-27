package tn.enicarthage.gestionSalleDeSport.service;

import tn.enicarthage.gestionSalleDeSport.dao.SalleDao;
import tn.enicarthage.gestionSalleDeSport.dto.SalleRequest;
import tn.enicarthage.gestionSalleDeSport.exception.ConflictException;
import tn.enicarthage.gestionSalleDeSport.exception.ResourceNotFoundException;
import tn.enicarthage.gestionSalleDeSport.model.Salle;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

public class SalleService {

    private final SalleDao salleDao = new SalleDao();

    public List<Salle> getSalles() throws SQLException {
        return salleDao.getSalles();
    }

    public Salle getSalle(int id) throws SQLException {
        Salle salle = salleDao.getSalle(id);
        if (salle == null) {
            throw new ResourceNotFoundException("Salle introuvable : " + id);
        }
        return salle;
    }

    public Salle createSalle(SalleRequest request) throws SQLException {
        valider(request);
        Salle salle = new Salle(0, request.nom().trim(), request.capacite());
        salle.setId(salleDao.createSalle(salle));
        return salle;
    }

    public Salle updateSalle(int id, SalleRequest request) throws SQLException {
        valider(request);
        Salle salle = new Salle(id, request.nom().trim(), request.capacite());
        if (!salleDao.updateSalle(salle)) {
            throw new ResourceNotFoundException("Salle introuvable : " + id);
        }
        return salle;
    }

    public void deleteSalle(int id) throws SQLException {
        try {
            if (!salleDao.deleteSalle(id)) {
                throw new ResourceNotFoundException("Salle introuvable : " + id);
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            // Cle etrangere cours.salle_id
            throw new ConflictException("Impossible de supprimer cette salle : des cours y sont programmes.");
        }
    }

    private void valider(SalleRequest request) {
        if (request.nom() == null || request.nom().isBlank()) {
            throw new IllegalArgumentException("Le nom de la salle est obligatoire.");
        }
        if (request.capacite() == null || request.capacite() <= 0) {
            throw new IllegalArgumentException("La capacite doit etre un nombre positif.");
        }
    }
}
