package tn.enicarthage.gestionSalleDeSport.model;

// Adherent inscrit a un cours (liste vue par l'admin et le coach)
public class Participant {

    private int id; // id de la table adherent
    private String nom;
    private String prenom;
    private String email;
    private String photo;

    public Participant(int id, String nom, String prenom, String email, String photo) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.photo = photo;
    }

    public int getId() { return id; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public String getEmail() { return email; }
    public String getPhoto() { return photo; }
}
