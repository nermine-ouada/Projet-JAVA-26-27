package tn.enicarthage.gestionSalleDeSport.dao;

import tn.enicarthage.gestionSalleDeSport.config.DatabaseConnection;
import tn.enicarthage.gestionSalleDeSport.model.Salle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SalleDao {

    public List<Salle> getSalles() throws SQLException {
        List<Salle> salles = new ArrayList<>();
        try (Connection cnx = DatabaseConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement("SELECT id, nom, capacite FROM salle ORDER BY nom");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                salles.add(mapper(rs));
            }
        }
        return salles;
    }

    public Salle getSalle(int id) throws SQLException {
        try (Connection cnx = DatabaseConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement("SELECT id, nom, capacite FROM salle WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapper(rs) : null;
            }
        }
    }

    public int createSalle(Salle salle) throws SQLException {
        try (Connection cnx = DatabaseConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(
                     "INSERT INTO salle (nom, capacite) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, salle.getNom());
            ps.setInt(2, salle.getCapacite());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    // Renvoie true si une ligne a ete modifiee
    public boolean updateSalle(Salle salle) throws SQLException {
        try (Connection cnx = DatabaseConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement("UPDATE salle SET nom = ?, capacite = ? WHERE id = ?")) {
            ps.setString(1, salle.getNom());
            ps.setInt(2, salle.getCapacite());
            ps.setInt(3, salle.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteSalle(int id) throws SQLException {
        try (Connection cnx = DatabaseConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement("DELETE FROM salle WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Salle mapper(ResultSet rs) throws SQLException {
        return new Salle(rs.getInt("id"), rs.getString("nom"), rs.getInt("capacite"));
    }
}
