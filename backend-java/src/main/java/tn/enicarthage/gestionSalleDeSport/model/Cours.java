package tn.enicarthage.gestionSalleDeSport.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Cours {

    private int id;
    private String nomCours;
    private LocalDate dateCours;
    private LocalTime heure;
    private int capacite;
    private int coachId;
    private int salleId;
    // Remplis par jointure, pour l'affichage
    private String coachNom;
    private String salleNom;

    public Cours() {
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNomCours() { return nomCours; }
    public void setNomCours(String nomCours) { this.nomCours = nomCours; }
    public LocalDate getDateCours() { return dateCours; }
    public void setDateCours(LocalDate dateCours) { this.dateCours = dateCours; }
    public LocalTime getHeure() { return heure; }
    public void setHeure(LocalTime heure) { this.heure = heure; }
    public int getCapacite() { return capacite; }
    public void setCapacite(int capacite) { this.capacite = capacite; }
    public int getCoachId() { return coachId; }
    public void setCoachId(int coachId) { this.coachId = coachId; }
    public int getSalleId() { return salleId; }
    public void setSalleId(int salleId) { this.salleId = salleId; }
    public String getCoachNom() { return coachNom; }
    public void setCoachNom(String coachNom) { this.coachNom = coachNom; }
    public String getSalleNom() { return salleNom; }
    public void setSalleNom(String salleNom) { this.salleNom = salleNom; }
}
