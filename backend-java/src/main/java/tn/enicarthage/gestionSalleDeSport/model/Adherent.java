package tn.enicarthage.gestionSalleDeSport.model;

// Adherent = un utilisateur avec le role ADHERENT (table adherent)
public class Adherent {

    private int id; // id de la table adherent (pas celui de utilisateur)
    private String nom;
    private String prenom;
    private String email;

    public Adherent() {
    }

    public Adherent(int id, String nom, String prenom, String email) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
