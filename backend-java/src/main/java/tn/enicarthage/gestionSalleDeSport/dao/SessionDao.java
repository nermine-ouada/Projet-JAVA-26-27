package tn.enicarthage.gestionSalleDeSport.dao;

import tn.enicarthage.gestionSalleDeSport.config.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SessionDao {

    // L'expiration est calculee par MySQL (NOW() + n jours) : meme horloge que la verification
    public void createSession(int utilisateurId, String token, int dureeJours) throws SQLException {
        String sql = "INSERT INTO session (utilisateur_id, token, expire_le) "
                + "VALUES (?, ?, DATE_ADD(NOW(), INTERVAL ? DAY))";
        try (Connection cnx = DatabaseConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, utilisateurId);
            ps.setString(2, token);
            ps.setInt(3, dureeJours);
            ps.executeUpdate();
        }
    }

    // Renvoie l'id de l'utilisateur si le token existe et n'est pas expire, sinon null
    public Integer getUtilisateurIdByToken(String token) throws SQLException {
        String sql = "SELECT utilisateur_id FROM session WHERE token = ? AND expire_le > NOW()";
        try (Connection cnx = DatabaseConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, token);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("utilisateur_id") : null;
            }
        }
    }

    public void deleteSession(String token) throws SQLException {
        try (Connection cnx = DatabaseConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement("DELETE FROM session WHERE token = ?")) {
            ps.setString(1, token);
            ps.executeUpdate();
        }
    }
}
