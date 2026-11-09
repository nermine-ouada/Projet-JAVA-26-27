package tn.enicarthage.gestionSalleDeSport.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Abonnement {

    private int id;
    private String type;          // Mensuel, Trimestriel ou Annuel
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private BigDecimal prix;      // BigDecimal : pas d'erreur d'arrondi sur l'argent
    private int adherentId;
    // Pour l'affichage
    private String adherentNom;   // rempli par jointure
    private String statut;        // ACTIF, EXPIRE ou A_VENIR (calcule par le service)

    public Abonnement() {
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }
    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }
    public BigDecimal getPrix() { return prix; }
    public void setPrix(BigDecimal prix) { this.prix = prix; }
    public int getAdherentId() { return adherentId; }
    public void setAdherentId(int adherentId) { this.adherentId = adherentId; }
    public String getAdherentNom() { return adherentNom; }
    public void setAdherentNom(String adherentNom) { this.adherentNom = adherentNom; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
}
