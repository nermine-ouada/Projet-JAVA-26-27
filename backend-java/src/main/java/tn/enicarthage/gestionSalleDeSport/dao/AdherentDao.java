package tn.enicarthage.gestionSalleDeSport.dao;

import tn.enicarthage.gestionSalleDeSport.config.DatabaseConnection;
import tn.enicarthage.gestionSalleDeSport.model.Adherent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// Lecture seule : les adherents sont crees via l'inscription ou le CRUD utilisateurs
public class AdherentDao {

    private static final String SELECT = """
            SELECT a.id, u.nom, u.prenom, u.email
            FROM adherent a
            JOIN utilisateur u ON u.id = a.utilisateur_id
            """;

    public List<Adherent> getAdherents() throws SQLException {
        List<Adherent> adherents = new ArrayList<>();
        try (Connection cnx = DatabaseConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(SELECT + " ORDER BY u.nom, u.prenom");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                adherents.add(mapper(rs));
            }
        }
        return adherents;
    }

    public Adherent getAdherent(int id) throws SQLException {
        try (Connection cnx = DatabaseConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(SELECT + " WHERE a.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapper(rs) : null;
            }
        }
    }

    // Retrouve l'adherent a partir de l'utilisateur connecte (null si ce n'est pas un adherent)
    public Adherent getAdherentByUtilisateur(int utilisateurId) throws SQLException {
        try (Connection cnx = DatabaseConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(SELECT + " WHERE a.utilisateur_id = ?")) {
            ps.setInt(1, utilisateurId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapper(rs) : null;
            }
        }
    }

    private Adherent mapper(ResultSet rs) throws SQLException {
        return new Adherent(rs.getInt("id"), rs.getString("nom"), rs.getString("prenom"), rs.getString("email"));
    }
}
