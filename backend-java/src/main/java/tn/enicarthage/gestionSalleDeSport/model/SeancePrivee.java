package tn.enicarthage.gestionSalleDeSport.model;

import java.time.LocalDate;
import java.time.LocalTime;

// Seance de coaching prive : un coach, un adherent, un creneau
public class SeancePrivee {

    private int id;
    private int coachId;
    private int adherentId;
    private LocalDate dateSeance;
    private LocalTime heure;
    private int dureeMinutes;
    private String objectif;
    private String statut; // EN_ATTENTE, CONFIRMEE ou ANNULEE
    // Remplis par jointure, pour l'affichage et les droits
    private String coachNom;
    private String coachPhoto;
    private String coachSpecialite;
    private int coachUtilisateurId;
    private String adherentNom;
    private String adherentPhoto;
    private int adherentUtilisateurId;

    public SeancePrivee() {
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getCoachId() { return coachId; }
    public void setCoachId(int coachId) { this.coachId = coachId; }
    public int getAdherentId() { return adherentId; }
    public void setAdherentId(int adherentId) { this.adherentId = adherentId; }
    public LocalDate getDateSeance() { return dateSeance; }
    public void setDateSeance(LocalDate dateSeance) { this.dateSeance = dateSeance; }
    public LocalTime getHeure() { return heure; }
    public void setHeure(LocalTime heure) { this.heure = heure; }
    public int getDureeMinutes() { return dureeMinutes; }
    public void setDureeMinutes(int dureeMinutes) { this.dureeMinutes = dureeMinutes; }
    public String getObjectif() { return objectif; }
    public void setObjectif(String objectif) { this.objectif = objectif; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public String getCoachNom() { return coachNom; }
    public void setCoachNom(String coachNom) { this.coachNom = coachNom; }
    public String getCoachPhoto() { return coachPhoto; }
    public void setCoachPhoto(String coachPhoto) { this.coachPhoto = coachPhoto; }
    public String getCoachSpecialite() { return coachSpecialite; }
    public void setCoachSpecialite(String coachSpecialite) { this.coachSpecialite = coachSpecialite; }
    public int getCoachUtilisateurId() { return coachUtilisateurId; }
    public void setCoachUtilisateurId(int coachUtilisateurId) { this.coachUtilisateurId = coachUtilisateurId; }
    public String getAdherentNom() { return adherentNom; }
    public void setAdherentNom(String adherentNom) { this.adherentNom = adherentNom; }
    public String getAdherentPhoto() { return adherentPhoto; }
    public void setAdherentPhoto(String adherentPhoto) { this.adherentPhoto = adherentPhoto; }
    public int getAdherentUtilisateurId() { return adherentUtilisateurId; }
    public void setAdherentUtilisateurId(int adherentUtilisateurId) { this.adherentUtilisateurId = adherentUtilisateurId; }
}
