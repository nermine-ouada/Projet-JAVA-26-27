package tn.enicarthage.gestionSalleDeSport.dao;

import tn.enicarthage.gestionSalleDeSport.config.DatabaseConnection;
import tn.enicarthage.gestionSalleDeSport.model.Participant;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Inscriptions des adherents aux cours (table inscription_cours)
public class InscriptionDao {

    // id de la table adherent a partir de l'utilisateur connecte (null si ce n'est pas un adherent)
    public Integer getAdherentId(int utilisateurId) throws SQLException {
        try (Connection cnx = DatabaseConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement("SELECT id FROM adherent WHERE utilisateur_id = ?")) {
            ps.setInt(1, utilisateurId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("id") : null;
            }
        }
    }

    public int countInscrits(int coursId) throws SQLException {
        try (Connection cnx = DatabaseConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement("SELECT COUNT(*) FROM inscription_cours WHERE cours_id = ?")) {
            ps.setInt(1, coursId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public boolean existeInscription(int coursId, int adherentId) throws SQLException {
        try (Connection cnx = DatabaseConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(
                     "SELECT 1 FROM inscription_cours WHERE cours_id = ? AND adherent_id = ?")) {
            ps.setInt(1, coursId);
            ps.setInt(2, adherentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void createInscription(int coursId, int adherentId) throws SQLException {
        try (Connection cnx = DatabaseConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(
                     "INSERT INTO inscription_cours (cours_id, adherent_id) VALUES (?, ?)")) {
            ps.setInt(1, coursId);
            ps.setInt(2, adherentId);
            ps.executeUpdate();
        }
    }

    public boolean deleteInscription(int coursId, int adherentId) throws SQLException {
        try (Connection cnx = DatabaseConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(
                     "DELETE FROM inscription_cours WHERE cours_id = ? AND adherent_id = ?")) {
            ps.setInt(1, coursId);
            ps.setInt(2, adherentId);
            return ps.executeUpdate() > 0;
        }
    }

    // id des cours auxquels un adherent est inscrit
    public Set<Integer> getCoursIdsByAdherent(int adherentId) throws SQLException {
        Set<Integer> ids = new HashSet<>();
        try (Connection cnx = DatabaseConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement("SELECT cours_id FROM inscription_cours WHERE adherent_id = ?")) {
            ps.setInt(1, adherentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("cours_id"));
                }
            }
        }
        return ids;
    }

    public List<Participant> getParticipants(int coursId) throws SQLException {
        String sql = """
                SELECT a.id, u.nom, u.prenom, u.email, u.photo
                FROM inscription_cours ic
                JOIN adherent a ON a.id = ic.adherent_id
                JOIN utilisateur u ON u.id = a.utilisateur_id
                WHERE ic.cours_id = ?
                ORDER BY u.nom, u.prenom
                """;
        List<Participant> liste = new ArrayList<>();
        try (Connection cnx = DatabaseConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, coursId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    liste.add(new Participant(rs.getInt("id"), rs.getString("nom"), rs.getString("prenom"),
                            rs.getString("email"), rs.getString("photo")));
                }
            }
        }
        return liste;
    }
}
