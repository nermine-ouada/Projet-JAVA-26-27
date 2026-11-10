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

    // Le role est deduit de la table qui contient l'utilisateur (administrateur / coach / adherent).
    // Telephone et specialite sont stockes dans la table du role.
    private static final String SELECT_AVEC_ROLE = """
            SELECT u.id, u.nom, u.prenom, u.email, u.mot_de_passe, u.photo,
                   CASE WHEN a.id IS NOT NULL THEN 'ADMIN'
                        WHEN c.id IS NOT NULL THEN 'COACH'
                        ELSE 'ADHERENT' END AS role,
                   COALESCE(c.telephone, ad.telephone) AS telephone,
                   c.specialite
            FROM utilisateur u
            LEFT JOIN administrateur a ON a.utilisateur_id = u.id
            LEFT JOIN coach c ON c.utilisateur_id = u.id
            LEFT JOIN adherent ad ON ad.utilisateur_id = u.id
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
                        "INSERT INTO utilisateur (nom, prenom, email, mot_de_passe, photo) VALUES (?, ?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, u.getNom());
                    ps.setString(2, u.getPrenom());
                    ps.setString(3, u.getEmail());
                    ps.setString(4, u.getMotDePasse());
                    ps.setString(5, u.getPhoto());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        id = keys.getInt(1);
                    }
                }
                try (PreparedStatement ps = cnx.prepareStatement(insertRole(u.getRole()))) {
                    ps.setInt(1, id);
                    if (u.getRole().equals("COACH")) {
                        ps.setString(2, u.getTelephone());
                        ps.setString(3, u.getSpecialite());
                    } else if (u.getRole().equals("ADHERENT")) {
                        ps.setString(2, u.getTelephone());
                    }
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

    // Modifie nom, prenom, email (+ mot de passe et photo s'ils sont fournis, non null),
    // puis telephone / specialite dans la table du role. Une seule transaction.
    public boolean updateUtilisateur(Utilisateur u) throws SQLException {
        boolean avecMotDePasse = u.getMotDePasse() != null;
        boolean avecPhoto = u.getPhoto() != null;
        String sql = "UPDATE utilisateur SET nom = ?, prenom = ?, email = ?"
                + (avecMotDePasse ? ", mot_de_passe = ?" : "")
                + (avecPhoto ? ", photo = ?" : "")
                + " WHERE id = ?";
        try (Connection cnx = DatabaseConnection.getConnection()) {
            cnx.setAutoCommit(false);
            try {
                boolean modifie;
                try (PreparedStatement ps = cnx.prepareStatement(sql)) {
                    int i = 1;
                    ps.setString(i++, u.getNom());
                    ps.setString(i++, u.getPrenom());
                    ps.setString(i++, u.getEmail());
                    if (avecMotDePasse) ps.setString(i++, u.getMotDePasse());
                    if (avecPhoto) ps.setString(i++, u.getPhoto().isEmpty() ? null : u.getPhoto());
                    ps.setInt(i, u.getId());
                    modifie = ps.executeUpdate() > 0;
                }
                // Une seule de ces deux requetes touche une ligne (selon le role)
                try (PreparedStatement ps = cnx.prepareStatement(
                        "UPDATE coach SET telephone = ?, specialite = ? WHERE utilisateur_id = ?")) {
                    ps.setString(1, u.getTelephone());
                    ps.setString(2, u.getSpecialite());
                    ps.setInt(3, u.getId());
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = cnx.prepareStatement(
                        "UPDATE adherent SET telephone = ? WHERE utilisateur_id = ?")) {
                    ps.setString(1, u.getTelephone());
                    ps.setInt(2, u.getId());
                    ps.executeUpdate();
                }
                cnx.commit();
                return modifie;
            } catch (SQLException e) {
                cnx.rollback();
                throw e;
            }
        }
    }

    // photo = null pour la supprimer
    public void updatePhoto(int id, String photo) throws SQLException {
        try (Connection cnx = DatabaseConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement("UPDATE utilisateur SET photo = ? WHERE id = ?")) {
            ps.setString(1, photo);
            ps.setInt(2, id);
            ps.executeUpdate();
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

    // Requete fixe par role (jamais construite a partir d'une saisie) : pas de risque d'injection SQL
    private String insertRole(String role) {
        return switch (role) {
            case "ADMIN" -> "INSERT INTO administrateur (utilisateur_id) VALUES (?)";
            case "COACH" -> "INSERT INTO coach (utilisateur_id, telephone, specialite) VALUES (?, ?, ?)";
            case "ADHERENT" -> "INSERT INTO adherent (utilisateur_id, telephone) VALUES (?, ?)";
            default -> throw new IllegalArgumentException("Role invalide : " + role);
        };
    }

    private Utilisateur mapper(ResultSet rs) throws SQLException {
        Utilisateur u = new Utilisateur(
                rs.getInt("id"),
                rs.getString("nom"),
                rs.getString("prenom"),
                rs.getString("email"),
                rs.getString("mot_de_passe"),
                rs.getString("role"));
        u.setPhoto(rs.getString("photo"));
        u.setTelephone(rs.getString("telephone"));
        u.setSpecialite(rs.getString("specialite"));
        return u;
    }
}
