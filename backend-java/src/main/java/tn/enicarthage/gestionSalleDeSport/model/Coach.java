package tn.enicarthage.gestionSalleDeSport.model;

// Coach = un utilisateur + sa specialite (table coach)
public class Coach {

    private int id; // id de la table coach (pas celui de utilisateur)
    private String nom;
    private String prenom;
    private String specialite;
    private String photo;

    public Coach() {
    }

    public Coach(int id, String nom, String prenom, String specialite, String photo) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.specialite = specialite;
        this.photo = photo;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getSpecialite() { return specialite; }
    public void setSpecialite(String specialite) { this.specialite = specialite; }
    public String getPhoto() { return photo; }
    public void setPhoto(String photo) { this.photo = photo; }
}
