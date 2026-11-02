package tn.enicarthage.gestionSalleDeSport.service;

import tn.enicarthage.gestionSalleDeSport.dao.UtilisateurDao;
import tn.enicarthage.gestionSalleDeSport.dto.UtilisateurRequest;
import tn.enicarthage.gestionSalleDeSport.dto.UtilisateurResponse;
import tn.enicarthage.gestionSalleDeSport.exception.ConflictException;
import tn.enicarthage.gestionSalleDeSport.exception.ResourceNotFoundException;
import tn.enicarthage.gestionSalleDeSport.model.Utilisateur;
import tn.enicarthage.gestionSalleDeSport.util.PasswordUtil;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class UtilisateurService {

    private static final Set<String> ROLES = Set.of("ADMIN", "COACH", "ADHERENT");

    private final UtilisateurDao utilisateurDao = new UtilisateurDao();

    public List<UtilisateurResponse> getUtilisateurs() throws SQLException {
        List<UtilisateurResponse> liste = new ArrayList<>();
        for (Utilisateur u : utilisateurDao.getUtilisateurs()) {
            liste.add(versResponse(u));
        }
        return liste;
    }

    public UtilisateurResponse getUtilisateur(int id) throws SQLException {
        return versResponse(trouver(id));
    }

    public UtilisateurResponse createUtilisateur(UtilisateurRequest r) throws SQLException {
        validerChamps(r.nom(), r.prenom(), r.email());
        validerMotDePasse(r.motDePasse());
        if (r.role() == null || !ROLES.contains(r.role())) {
            throw new IllegalArgumentException("Role invalide (ADMIN, COACH ou ADHERENT).");
        }
        String email = normaliserEmail(r.email());
        if (utilisateurDao.getUtilisateurByEmail(email) != null) {
            throw new ConflictException("Un compte existe deja avec cet email.");
        }

        Utilisateur u = new Utilisateur(0, r.nom().trim(), r.prenom().trim(), email,
                PasswordUtil.hasher(r.motDePasse()), r.role());
        int id = utilisateurDao.createUtilisateur(u);
        return getUtilisateur(id);
    }

    public UtilisateurResponse updateUtilisateur(int id, UtilisateurRequest r) throws SQLException {
        trouver(id); // 404 si l'utilisateur n'existe pas
        validerChamps(r.nom(), r.prenom(), r.email());
        String email = normaliserEmail(r.email());
        Utilisateur memeEmail = utilisateurDao.getUtilisateurByEmail(email);
        if (memeEmail != null && memeEmail.getId() != id) {
            throw new ConflictException("Un autre compte utilise deja cet email.");
        }

        // Mot de passe vide = on garde l'ancien
        String hash = null;
        if (r.motDePasse() != null && !r.motDePasse().isEmpty()) {
            validerMotDePasse(r.motDePasse());
            hash = PasswordUtil.hasher(r.motDePasse());
        }
        utilisateurDao.updateUtilisateur(new Utilisateur(id, r.nom().trim(), r.prenom().trim(), email, hash, null));
        return getUtilisateur(id);
    }

    public void deleteUtilisateur(int id, int idConnecte) throws SQLException {
        if (id == idConnecte) {
            throw new IllegalArgumentException("Vous ne pouvez pas supprimer votre propre compte.");
        }
        try {
            if (!utilisateurDao.deleteUtilisateur(id)) {
                throw new ResourceNotFoundException("Utilisateur introuvable : " + id);
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            // Cle etrangere : un coach reference par des cours ne peut pas etre supprime
            throw new ConflictException("Impossible de supprimer cet utilisateur : il anime encore des cours.");
        }
    }

    private Utilisateur trouver(int id) throws SQLException {
        Utilisateur u = utilisateurDao.getUtilisateur(id);
        if (u == null) {
            throw new ResourceNotFoundException("Utilisateur introuvable : " + id);
        }
        return u;
    }

    private void validerChamps(String nom, String prenom, String email) {
        if (estVide(nom) || estVide(prenom) || estVide(email)) {
            throw new IllegalArgumentException("Nom, prenom et email obligatoires.");
        }
        if (!email.trim().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("Email invalide.");
        }
    }

    private void validerMotDePasse(String motDePasse) {
        if (motDePasse == null || motDePasse.length() < 6) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins 6 caracteres.");
        }
    }

    private String normaliserEmail(String email) {
        return email.trim().toLowerCase();
    }

    private boolean estVide(String valeur) {
        return valeur == null || valeur.isBlank();
    }

    // Ne renvoie jamais le mot de passe (meme hashe) au client
    static UtilisateurResponse versResponse(Utilisateur u) {
        return new UtilisateurResponse(u.getId(), u.getNom(), u.getPrenom(), u.getEmail(), u.getRole());
    }
}
