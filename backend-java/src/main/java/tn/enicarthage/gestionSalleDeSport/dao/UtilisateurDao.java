package tn.enicarthage.gestionSalleDeSport.dao;

import tn.enicarthage.gestionSalleDeSport.config.DatabaseConnection;
import tn.enicarthage.gestionSalleDeSport.model.Utilisateur;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurDao {

    // Le role est deduit de la table qui contient l'utilisateur (administrateur / coach / adherent)
    private static final String SELECT_AVEC_ROLE = """
            SELECT u.id, u.nom, u.prenom, u.email, u.mot_de_passe,
                   CASE WHEN a.id IS NOT NULL THEN 'ADMIN'
                        WHEN c.id IS NOT NULL THEN 'COACH'
                        ELSE 'ADHERENT' END AS role
            FROM utilisateur u
            LEFT JOIN administrateur a ON a.utilisateur_id = u.id
            LEFT JOIN coach c ON c.utilisateur_id = u.id
            """;

    public List<Utilisateur> getUtilisateurs() throws SQLException {
        List<Utilisateur> utilisateurs = new ArrayList<>();
        try (Connection cnx = DatabaseConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(SELECT_AVEC_ROLE + " ORDER BY u.nom, u.prenom");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                utilisateurs.add(mapper(rs));
            }
        }
        return utilisateurs;
    }

    public Utilisateur getUtilisateurByEmail(String email) throws SQLException {
        String sql = SELECT_AVEC_ROLE + " WHERE u.email = ?";
        try (Connection cnx = DatabaseConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapper(rs) : null;
            }
        }
    }

    public Utilisateur getUtilisateur(int id) throws SQLException {
        String sql = SELECT_AVEC_ROLE + " WHERE u.id = ?";
        try (Connection cnx = DatabaseConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapper(rs) : null;
            }
        }
    }

    // Insere dans utilisateur PUIS dans la table du role, dans une seule transaction.
    // Si la 2e insertion echoue, la 1re est annulee (rollback).
    public int createUtilisateur(Utilisateur u) throws SQLException {
        try (Connection cnx = DatabaseConnection.getConnection()) {
            cnx.setAutoCommit(false);
            try {
                int id;
                try (PreparedStatement ps = cnx.prepareStatement(
                        "INSERT INTO utilisateur (nom, prenom, email, mot_de_passe) VALUES (?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, u.getNom());
                    ps.setString(2, u.getPrenom());
                    ps.setString(3, u.getEmail());
                    ps.setString(4, u.getMotDePasse());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        id = keys.getInt(1);
                    }
                }
                try (PreparedStatement ps = cnx.prepareStatement(
                        "INSERT INTO " + tableDuRole(u.getRole()) + " (utilisateur_id) VALUES (?)")) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
                cnx.commit();
                return id;
            } catch (SQLException e) {
                cnx.rollback();
                throw e;
            }
        }
    }

    // Modifie nom, prenom, email ; le mot de passe seulement s'il est fourni (non null)
    public boolean updateUtilisateur(Utilisateur u) throws SQLException {
        boolean avecMotDePasse = u.getMotDePasse() != null;
        String sql = "UPDATE utilisateur SET nom = ?, prenom = ?, email = ?"
                + (avecMotDePasse ? ", mot_de_passe = ?" : "")
                + " WHERE id = ?";
        try (Connection cnx = DatabaseConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, u.getNom());
            ps.setString(2, u.getPrenom());
            ps.setString(3, u.getEmail());
            if (avecMotDePasse) {
                ps.setString(4, u.getMotDePasse());
                ps.setInt(5, u.getId());
            } else {
                ps.setInt(4, u.getId());
            }
            return ps.executeUpdate() > 0;
        }
    }

    // ON DELETE CASCADE supprime aussi la ligne du role et les sessions
    public boolean deleteUtilisateur(int id) throws SQLException {
        try (Connection cnx = DatabaseConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement("DELETE FROM utilisateur WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // Nom de table fixe (jamais une saisie de l'utilisateur) : pas de risque d'injection SQL
    private String tableDuRole(String role) {
        return switch (role) {
            case "ADMIN" -> "administrateur";
            case "COACH" -> "coach";
            case "ADHERENT" -> "adherent";
            default -> throw new IllegalArgumentException("Role invalide : " + role);
        };
    }

    private Utilisateur mapper(ResultSet rs) throws SQLException {
        return new Utilisateur(
                rs.getInt("id"),
                rs.getString("nom"),
                rs.getString("prenom"),
                rs.getString("email"),
                rs.getString("mot_de_passe"),
                rs.getString("role"));
    }
}
