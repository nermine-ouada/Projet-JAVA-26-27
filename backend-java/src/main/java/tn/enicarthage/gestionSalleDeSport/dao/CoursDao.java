package tn.enicarthage.gestionSalleDeSport.dao;

import tn.enicarthage.gestionSalleDeSport.config.DatabaseConnection;
import tn.enicarthage.gestionSalleDeSport.model.Cours;

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

public class CoursDao {

    // Jointure pour recuperer le coach (nom, photo) et la salle,
    // + sous-requete qui compte les inscrits de chaque cours
    private static final String SELECT = """
            SELECT co.id, co.nom_cours, co.date_cours, co.heure, co.capacite,
                   co.coach_id, co.salle_id,
                   CONCAT(u.prenom, ' ', u.nom) AS coach_nom,
                   u.photo AS coach_photo,
                   c.utilisateur_id AS coach_utilisateur_id,
                   s.nom AS salle_nom,
                   (SELECT COUNT(*) FROM inscription_cours ic WHERE ic.cours_id = co.id) AS inscrits
            FROM cours co
            JOIN coach c ON c.id = co.coach_id
            JOIN utilisateur u ON u.id = c.utilisateur_id
            JOIN salle s ON s.id = co.salle_id
            """;

    // Duree d'un cours collectif : la salle est occupee 1 heure a partir de l'heure du cours
    public static final int DUREE_MINUTES = 60;

    // Cours qui occupe deja la salle sur ce creneau (sauf le cours "exclu"), ou null si la salle est libre.
    // Deux cours se chevauchent s'ils commencent a moins d'une heure d'ecart, le meme jour.
    public Cours getCoursEnConflit(int salleId, LocalDate date, LocalTime heure, int coursExcluId) throws SQLException {
        String sql = SELECT + """
                 WHERE co.salle_id = ? AND co.date_cours = ? AND co.id <> ?
                   AND ABS(TIME_TO_SEC(TIMEDIFF(co.heure, ?))) < ?
                 ORDER BY co.heure
                """;
        try (Connection cnx = DatabaseConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, salleId);
            ps.setDate(2, Date.valueOf(date));
            ps.setInt(3, coursExcluId);
            ps.setTime(4, Time.valueOf(heure));
            ps.setInt(5, DUREE_MINUTES * 60);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapper(rs) : null;
            }
        }
    }

    public List<Cours> getAllCours() throws SQLException {
        List<Cours> liste = new ArrayList<>();
        try (Connection cnx = DatabaseConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(SELECT + " ORDER BY co.date_cours, co.heure");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                liste.add(mapper(rs));
            }
        }
        return liste;
    }

    public Cours getCours(int id) throws SQLException {
        try (Connection cnx = DatabaseConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(SELECT + " WHERE co.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapper(rs) : null;
            }
        }
    }

    public int createCours(Cours cours) throws SQLException {
        String sql = "INSERT INTO cours (nom_cours, date_cours, heure, capacite, coach_id, salle_id) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection cnx = DatabaseConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            remplir(ps, cours);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    public boolean updateCours(Cours cours) throws SQLException {
        String sql = "UPDATE cours SET nom_cours = ?, date_cours = ?, heure = ?, capacite = ?, "
                + "coach_id = ?, salle_id = ? WHERE id = ?";
        try (Connection cnx = DatabaseConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            remplir(ps, cours);
            ps.setInt(7, cours.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteCours(int id) throws SQLException {
        try (Connection cnx = DatabaseConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement("DELETE FROM cours WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // Les 6 premiers "?" sont les memes pour INSERT et UPDATE
    private void remplir(PreparedStatement ps, Cours cours) throws SQLException {
        ps.setString(1, cours.getNomCours());
        ps.setDate(2, Date.valueOf(cours.getDateCours()));
        ps.setTime(3, Time.valueOf(cours.getHeure()));
        ps.setInt(4, cours.getCapacite());
        ps.setInt(5, cours.getCoachId());
        ps.setInt(6, cours.getSalleId());
    }

    private Cours mapper(ResultSet rs) throws SQLException {
        Cours cours = new Cours();
        cours.setId(rs.getInt("id"));
        cours.setNomCours(rs.getString("nom_cours"));
        cours.setDateCours(rs.getDate("date_cours").toLocalDate());
        cours.setHeure(rs.getTime("heure").toLocalTime());
        cours.setCapacite(rs.getInt("capacite"));
        cours.setCoachId(rs.getInt("coach_id"));
        cours.setSalleId(rs.getInt("salle_id"));
        cours.setCoachNom(rs.getString("coach_nom"));
        cours.setCoachPhoto(rs.getString("coach_photo"));
        cours.setCoachUtilisateurId(rs.getInt("coach_utilisateur_id"));
        cours.setSalleNom(rs.getString("salle_nom"));
        cours.setInscrits(rs.getInt("inscrits"));
        return cours;
    }
}
