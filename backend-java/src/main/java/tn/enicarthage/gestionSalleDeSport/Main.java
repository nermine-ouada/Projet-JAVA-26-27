package tn.enicarthage.gestionSalleDeSport;

import tn.enicarthage.gestionSalleDeSport.config.DatabaseConnection;

import java.io.IOException;
import java.sql.Connection;

public class Main {

    private static final int PORT = 8081;

    public static void main(String[] args) {
        if (!testerConnexion()) {
            System.out.println("Verifiez que MySQL est demarre et que database.sql est execute.");
            return;
        }

        try {
            ApiServer serveur = new ApiServer(PORT);
            serveur.demarrer();
            System.out.println("Serveur HTTP demarre sur http://localhost:" + PORT);
            System.out.println("Testez avec Postman : POST http://localhost:" + PORT + "/auth/login");
        } catch (IOException e) {
            System.out.println("Impossible de demarrer le serveur : " + e.getMessage());
        }
    }

    private static boolean testerConnexion() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            System.out.println("Connexion reussie a MySQL.");
            return true;
        } catch (Exception e) {
            System.out.println("Erreur de connexion : " + e.getMessage());
            return false;
        }
    }
}
