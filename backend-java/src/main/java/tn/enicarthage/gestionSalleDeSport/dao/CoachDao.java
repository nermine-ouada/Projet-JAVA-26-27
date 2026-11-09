package tn.enicarthage.gestionSalleDeSport.dao;

import tn.enicarthage.gestionSalleDeSport.config.DatabaseConnection;
import tn.enicarthage.gestionSalleDeSport.model.Coach;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// Lecture seule : les coachs sont crees via le CRUD utilisateurs (role COACH)
public class CoachDao {

    private static final String SELECT = """
            SELECT c.id, u.nom, u.prenom, c.specialite
            FROM coach c
            JOIN utilisateur u ON u.id = c.utilisateur_id
            """;

    public List<Coach> getCoachs() throws SQLException {
        List<Coach> coachs = new ArrayList<>();
        try (Connection cnx = DatabaseConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(SELECT + " ORDER BY u.nom");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                coachs.add(mapper(rs));
            }
        }
        return coachs;
    }

    public Coach getCoach(int id) throws SQLException {
        try (Connection cnx = DatabaseConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(SELECT + " WHERE c.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapper(rs) : null;
            }
        }
    }

    private Coach mapper(ResultSet rs) throws SQLException {
        return new Coach(rs.getInt("id"), rs.getString("nom"), rs.getString("prenom"), rs.getString("specialite"));
    }
}
