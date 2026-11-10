package tn.enicarthage.gestionSalleDeSport.dao;

import tn.enicarthage.gestionSalleDeSport.config.DatabaseConnection;
import tn.enicarthage.gestionSalleDeSport.model.SeancePrivee;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

// Seances de coaching prive (table seance_privee)
public class SeanceDao {

    // Jointures pour le nom et la photo du coach et de l'adherent
    private static final String SELECT = """
            SELECT sp.id, sp.coach_id, sp.adherent_id, sp.date_seance, sp.heure, sp.duree_minutes,
                   sp.objectif, sp.statut,
                   CONCAT(uc.prenom, ' ', uc.nom) AS coach_nom, uc.photo AS coach_photo,
                   c.specialite AS coach_specialite, c.utilisateur_id AS coach_utilisateur_id,
                   CONCAT(ua.prenom, ' ', ua.nom) AS adherent_nom, ua.photo AS adherent_photo,
                   a.utilisateur_id AS adherent_utilisateur_id
            FROM seance_privee sp
            JOIN coach c ON c.id = sp.coach_id
            JOIN utilisateur uc ON uc.id = c.utilisateur_id
            JOIN adherent a ON a.id = sp.adherent_id
            JOIN utilisateur ua ON ua.id = a.utilisateur_id
            """;
    private static final String TRI = " ORDER BY sp.date_seance, sp.heure";

    public List<SeancePrivee> getSeances() throws SQLException {
        try (Connection cnx = DatabaseConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(SELECT + TRI)) {
            return lire(ps);
        }
    }

    // Seances d'un coach ou d'un adherent, a partir de son id utilisateur
    public List<SeancePrivee> getSeancesByUtilisateur(int utilisateurId) throws SQLException {
        String sql = SELECT + " WHERE c.utilisateur_id = ? OR a.utilisateur_id = ?" + TRI;
        try (Connection cnx = DatabaseConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, utilisateurId);
            ps.setInt(2, utilisateurId);
            return lire(ps);
        }
    }

    public SeancePrivee getSeance(int id) throws SQLException {
        try (Connection cnx = DatabaseConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(SELECT + " WHERE sp.id = ?")) {
            ps.setInt(1, id);
            List<SeancePrivee> liste = lire(ps);
            return liste.isEmpty() ? null : liste.get(0);
        }
    }

    // Le coach a-t-il deja une seance (non annulee) qui chevauche ce creneau ?
    // Deux creneaux se chevauchent si chacun commence avant la fin de l'autre.
    public boolean existeChevauchement(int coachId, LocalDate date, LocalTime debut, int dureeMinutes)
            throws SQLException {
        String sql = """
                SELECT 1 FROM seance_privee
                WHERE coach_id = ? AND date_seance = ? AND statut <> 'ANNULEE'
                  AND heure < ?
                  AND ADDTIME(heure, SEC_TO_TIME(duree_minutes * 60)) > ?
                """;
        try (Connection cnx = DatabaseConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, coachId);
            ps.setDate(2, Date.valueOf(date));
            ps.setTime(3, Time.valueOf(debut.plusMinutes(dureeMinutes)));
            ps.setTime(4, Time.valueOf(debut));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int createSeance(SeancePrivee s) throws SQLException {
        String sql = "INSERT INTO seance_privee (coach_id, adherent_id, date_seance, heure, duree_minutes, objectif, statut) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection cnx = DatabaseConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, s.getCoachId());
            ps.setInt(2, s.getAdherentId());
            ps.setDate(3, Date.valueOf(s.getDateSeance()));
            ps.setTime(4, Time.valueOf(s.getHeure()));
            ps.setInt(5, s.getDureeMinutes());
            ps.setString(6, s.getObjectif());
            ps.setString(7, s.getStatut());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    public void updateStatut(int id, String statut) throws SQLException {
        try (Connection cnx = DatabaseConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement("UPDATE seance_privee SET statut = ? WHERE id = ?")) {
            ps.setString(1, statut);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public boolean deleteSeance(int id) throws SQLException {
        try (Connection cnx = DatabaseConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement("DELETE FROM seance_privee WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private List<SeancePrivee> lire(PreparedStatement ps) throws SQLException {
        List<SeancePrivee> liste = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                SeancePrivee s = new SeancePrivee();
                s.setId(rs.getInt("id"));
                s.setCoachId(rs.getInt("coach_id"));
                s.setAdherentId(rs.getInt("adherent_id"));
                s.setDateSeance(rs.getDate("date_seance").toLocalDate());
                s.setHeure(rs.getTime("heure").toLocalTime());
                s.setDureeMinutes(rs.getInt("duree_minutes"));
                s.setObjectif(rs.getString("objectif"));
                s.setStatut(rs.getString("statut"));
                s.setCoachNom(rs.getString("coach_nom"));
                s.setCoachPhoto(rs.getString("coach_photo"));
                s.setCoachSpecialite(rs.getString("coach_specialite"));
                s.setCoachUtilisateurId(rs.getInt("coach_utilisateur_id"));
                s.setAdherentNom(rs.getString("adherent_nom"));
                s.setAdherentPhoto(rs.getString("adherent_photo"));
                s.setAdherentUtilisateurId(rs.getInt("adherent_utilisateur_id"));
                liste.add(s);
            }
        }
        return liste;
    }
}
