package tn.enicarthage.gestionSalleDeSport.dao;

import tn.enicarthage.gestionSalleDeSport.config.DatabaseConnection;
import tn.enicarthage.gestionSalleDeSport.model.Abonnement;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class AbonnementDao {

    // Jointure pour recuperer le nom de l'adherent
    private static final String SELECT = """
            SELECT ab.id, ab.type, ab.date_debut, ab.date_fin, ab.prix, ab.adherent_id,
                   CONCAT(u.prenom, ' ', u.nom) AS adherent_nom
            FROM abonnement ab
            JOIN adherent a ON a.id = ab.adherent_id
            JOIN utilisateur u ON u.id = a.utilisateur_id
            """;

    public List<Abonnement> getAbonnements() throws SQLException {
        try (Connection cnx = DatabaseConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(SELECT + " ORDER BY ab.date_fin DESC")) {
            return lire(ps);
        }
    }

    // Abonnements d'un seul adherent (page "Mon profil")
    public List<Abonnement> getAbonnementsByAdherent(int adherentId) throws SQLException {
        try (Connection cnx = DatabaseConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(SELECT + " WHERE ab.adherent_id = ? ORDER BY ab.date_fin DESC")) {
            ps.setInt(1, adherentId);
            return lire(ps);
        }
    }

    public Abonnement getAbonnement(int id) throws SQLException {
        try (Connection cnx = DatabaseConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(SELECT + " WHERE ab.id = ?")) {
            ps.setInt(1, id);
            List<Abonnement> liste = lire(ps);
            return liste.isEmpty() ? null : liste.get(0);
        }
    }

    public int createAbonnement(Abonnement abonnement) throws SQLException {
        String sql = "INSERT INTO abonnement (type, date_debut, date_fin, prix, adherent_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection cnx = DatabaseConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            remplir(ps, abonnement);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    public boolean updateAbonnement(Abonnement abonnement) throws SQLException {
        String sql = "UPDATE abonnement SET type = ?, date_debut = ?, date_fin = ?, prix = ?, adherent_id = ? WHERE id = ?";
        try (Connection cnx = DatabaseConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            remplir(ps, abonnement);
            ps.setInt(6, abonnement.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteAbonnement(int id) throws SQLException {
        try (Connection cnx = DatabaseConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement("DELETE FROM abonnement WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // Les 5 premiers "?" sont les memes pour INSERT et UPDATE
    private void remplir(PreparedStatement ps, Abonnement abonnement) throws SQLException {
        ps.setString(1, abonnement.getType());
        ps.setDate(2, Date.valueOf(abonnement.getDateDebut()));
        ps.setDate(3, Date.valueOf(abonnement.getDateFin()));
        ps.setBigDecimal(4, abonnement.getPrix());
        ps.setInt(5, abonnement.getAdherentId());
    }

    private List<Abonnement> lire(PreparedStatement ps) throws SQLException {
        List<Abonnement> liste = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Abonnement ab = new Abonnement();
                ab.setId(rs.getInt("id"));
                ab.setType(rs.getString("type"));
                ab.setDateDebut(rs.getDate("date_debut").toLocalDate());
                ab.setDateFin(rs.getDate("date_fin").toLocalDate());
                ab.setPrix(rs.getBigDecimal("prix"));
                ab.setAdherentId(rs.getInt("adherent_id"));
                ab.setAdherentNom(rs.getString("adherent_nom"));
                liste.add(ab);
            }
        }
        return liste;
    }
}
