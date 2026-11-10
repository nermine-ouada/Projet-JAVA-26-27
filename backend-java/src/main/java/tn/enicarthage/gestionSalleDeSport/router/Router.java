package tn.enicarthage.gestionSalleDeSport.router;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.sun.net.httpserver.HttpExchange;
// Imports avec * : un nouveau controller ou DTO est utilisable sans modifier cette liste
import tn.enicarthage.gestionSalleDeSport.controller.*;
import tn.enicarthage.gestionSalleDeSport.dto.*;
import tn.enicarthage.gestionSalleDeSport.exception.ConflictException;
import tn.enicarthage.gestionSalleDeSport.exception.ForbiddenException;
import tn.enicarthage.gestionSalleDeSport.exception.ResourceNotFoundException;
import tn.enicarthage.gestionSalleDeSport.exception.UnauthorizedException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

public class Router {

    // Represente une reponse HTTP (statut + corps JSON)
    private record Reponse(int statut, String corps) {
    }

    // Gson configure pour gerer LocalDate / LocalTime / LocalDateTime (format ISO)
    // ex: "2026-11-20", "18:30", "2026-11-20T18:30"
    // serializeNulls : un champ vide est renvoye en "photo": null au lieu de disparaitre du JSON
    private final Gson gson = new GsonBuilder()
            .serializeNulls()
            .registerTypeAdapter(LocalDate.class,
                    (JsonSerializer<LocalDate>) (src, type, ctx) -> new JsonPrimitive(src.toString()))
            .registerTypeAdapter(LocalDate.class,
                    (JsonDeserializer<LocalDate>) (json, type, ctx) ->
                            json.isJsonNull() ? null : LocalDate.parse(json.getAsString()))
            .registerTypeAdapter(LocalTime.class,
                    (JsonSerializer<LocalTime>) (src, type, ctx) -> new JsonPrimitive(src.toString()))
            .registerTypeAdapter(LocalTime.class,
                    (JsonDeserializer<LocalTime>) (json, type, ctx) ->
                            json.isJsonNull() ? null : LocalTime.parse(json.getAsString()))
            .registerTypeAdapter(LocalDateTime.class,
                    (JsonSerializer<LocalDateTime>) (src, type, ctx) -> new JsonPrimitive(src.toString()))
            .registerTypeAdapter(LocalDateTime.class,
                    (JsonDeserializer<LocalDateTime>) (json, type, ctx) ->
                            json.isJsonNull() ? null : LocalDateTime.parse(json.getAsString()))
            .create();

    private final AuthController authController = new AuthController();
    private final UtilisateurController utilisateurController = new UtilisateurController();
    private final CoachController coachController = new CoachController();
    // [eya] salles, cours

    // [nermine] abonnements
    private final AbonnementController abonnementController = new AbonnementController();
    private final SeanceController seanceController = new SeanceController();


    // Point d'entree unique pour toutes les requetes HTTP
    public void dispatcher(HttpExchange exchange) throws IOException {
        String methode = exchange.getRequestMethod();
        String chemin = exchange.getRequestURI().getPath();
        String[] segments = chemin.split("/");

        ajouterEntetesCors(exchange);
        // Requete "preflight" envoyee par le navigateur avant un POST/PUT/DELETE depuis React
        if (methode.equals("OPTIONS")) {
            envoyer(exchange, 204, "");
            return;
        }

        try {
            Reponse reponse = router(methode, segments, exchange);
            envoyer(exchange, reponse.statut(), reponse.corps());
        } catch (UnauthorizedException e) {
            envoyer(exchange, 401, messageErreur(e.getMessage()));
        } catch (ForbiddenException e) {
            envoyer(exchange, 403, messageErreur(e.getMessage()));
        } catch (ResourceNotFoundException e) {
            envoyer(exchange, 404, messageErreur(e.getMessage()));
        } catch (ConflictException e) {
            envoyer(exchange, 409, messageErreur(e.getMessage()));
        } catch (SQLIntegrityConstraintViolationException e) {
            // ex. supprimer un coach qui anime encore des cours
            envoyer(exchange, 409, messageErreur("Operation impossible : cet element est utilise ailleurs."));
        } catch (DateTimeParseException e) {
            envoyer(exchange, 400, messageErreur("Date ou heure invalide (format attendu : 2026-11-20 et 18:30)."));
        } catch (IllegalArgumentException | JsonParseException e) {
            envoyer(exchange, 400, messageErreur(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            envoyer(exchange, 500, messageErreur("Erreur interne du serveur."));
        }
    }

    // Dispatch par premiere ressource (/auth, /utilisateurs, /coachs, ...)
    private Reponse router(String methode, String[] segments, HttpExchange exchange)
            throws IOException, SQLException {
        if (segments.length < 2 || segments[1].isBlank()) {
            throw new ResourceNotFoundException("Route vide ou inconnue.");
        }

        switch (segments[1]) {
            case "auth":
                return routesAuth(methode, segments, exchange);
            case "utilisateurs":
                return routesUtilisateur(methode, segments, exchange);
            case "coachs":
                return routesCoach(methode, segments, exchange);
            // [eya] salles, cours

            // [nermine] abonnements
            case "abonnements":
                return routesAbonnement(methode, segments, exchange);
            case "adherents":
                return routesAdherent(methode, segments, exchange);
            case "seances":
                return routesSeance(methode, segments, exchange);

            default:
                throw new ResourceNotFoundException("Route inconnue : /" + segments[1]);
        }
    }

    // ---------------------------------------------------------
    // Authentification
    // POST /auth/register   (public)
    // POST /auth/login      (public)
    // POST /auth/logout     (connecte)
    // GET  /auth/me         (connecte)
    // PUT  /auth/photo      (connecte) -> changer sa photo
    // ---------------------------------------------------------
    private Reponse routesAuth(String methode, String[] segments, HttpExchange exchange)
            throws IOException, SQLException {
        if (segments.length != 3) {
            throw new ResourceNotFoundException("Route inconnue : " + String.join("/", segments));
        }
        switch (methode + " " + segments[2]) {
            case "POST register": return register(exchange);
            case "POST login":    return login(exchange);
            case "POST logout":   return logout(exchange);
            case "GET me":        return new Reponse(200, gson.toJson(getMe(exchange)));
            case "PUT photo":     return updateMaPhoto(exchange);
            default:
                throw new ResourceNotFoundException("Route inconnue : " + methode + " /auth/" + segments[2]);
        }
    }

    private Reponse register(HttpExchange exchange) throws IOException, SQLException {
        RegisterRequest request = lireCorps(exchange, RegisterRequest.class);
        return new Reponse(201, gson.toJson(authController.register(request)));
    }

    private Reponse login(HttpExchange exchange) throws IOException, SQLException {
        LoginRequest request = lireCorps(exchange, LoginRequest.class);
        return new Reponse(200, gson.toJson(authController.login(request)));
    }

    private Reponse logout(HttpExchange exchange) throws SQLException {
        authController.logout(token(exchange));
        return new Reponse(204, "");
    }

    // Utilisateur connecte (via le token). Leve 401 si pas connecte.
    private UtilisateurResponse getMe(HttpExchange exchange) throws SQLException {
        return authController.getMe(token(exchange));
    }

    private Reponse updateMaPhoto(HttpExchange exchange) throws IOException, SQLException {
        UtilisateurResponse connecte = getMe(exchange);
        PhotoRequest request = lireCorps(exchange, PhotoRequest.class);
        return new Reponse(200, gson.toJson(authController.updateMaPhoto(connecte.id(), request.photo())));
    }

    // GET /coachs (connecte) -> getCoachs : choix d'un coach (cours, seances privees)
    private Reponse routesCoach(String methode, String[] segments, HttpExchange exchange)
            throws SQLException {
        getMe(exchange);
        if (segments.length == 2 && methode.equals("GET")) {
            return getCoachs();
        }
        throw new ResourceNotFoundException("Route inconnue : " + methode + " " + String.join("/", segments));
    }

    private Reponse getCoachs() throws SQLException {
        return new Reponse(200, gson.toJson(coachController.getCoachs()));
    }

    // ---------------------------------------------------------
    // CRUD Utilisateurs (reserve a l'ADMIN)
    // GET    /utilisateurs        -> getUtilisateurs
    // GET    /utilisateurs/{id}   -> getUtilisateur
    // POST   /utilisateurs        -> createUtilisateur
    // PUT    /utilisateurs/{id}   -> updateUtilisateur
    // DELETE /utilisateurs/{id}   -> deleteUtilisateur
    // ---------------------------------------------------------
    private Reponse routesUtilisateur(String methode, String[] segments, HttpExchange exchange)
            throws IOException, SQLException {
        UtilisateurResponse connecte = getMe(exchange);
        exigerRole(connecte, "ADMIN");

        if (segments.length == 2) {
            switch (methode) {
                case "GET":  return getUtilisateurs();
                case "POST": return createUtilisateur(exchange);
                default:
                    throw new IllegalArgumentException("Methode non supportee : " + methode + " /utilisateurs");
            }
        }

        if (segments.length == 3) {
            int id = entier(segments[2]);
            switch (methode) {
                case "GET":    return getUtilisateur(id);
                case "PUT":    return updateUtilisateur(exchange, id);
                case "DELETE": return deleteUtilisateur(id, connecte);
                default:
                    throw new IllegalArgumentException("Methode non supportee : " + methode + " /utilisateurs/{id}");
            }
        }

        throw new ResourceNotFoundException("Route inconnue : " + String.join("/", segments));
    }

    private Reponse getUtilisateurs() throws SQLException {
        return new Reponse(200, gson.toJson(utilisateurController.getUtilisateurs()));
    }

    private Reponse getUtilisateur(int id) throws SQLException {
        return new Reponse(200, gson.toJson(utilisateurController.getUtilisateur(id)));
    }

    private Reponse createUtilisateur(HttpExchange exchange) throws IOException, SQLException {
        UtilisateurRequest request = lireCorps(exchange, UtilisateurRequest.class);
        return new Reponse(201, gson.toJson(utilisateurController.createUtilisateur(request)));
    }

    private Reponse updateUtilisateur(HttpExchange exchange, int id) throws IOException, SQLException {
        UtilisateurRequest request = lireCorps(exchange, UtilisateurRequest.class);
        return new Reponse(200, gson.toJson(utilisateurController.updateUtilisateur(id, request)));
    }

    private Reponse deleteUtilisateur(int id, UtilisateurResponse connecte) throws SQLException {
        utilisateurController.deleteUtilisateur(id, connecte.id());
        return new Reponse(204, "");
    }

    // =========================================================
    // [eya] Routes salles, cours

    // =========================================================
    // [nermine] Routes abonnements

    // ---------------------------------------------------------
    // CRUD Abonnements
    // GET    /abonnements        -> getAbonnements (ADMIN : tous, ADHERENT : les siens)
    // GET    /abonnements/{id}   -> getAbonnement     (ADMIN)
    // POST   /abonnements        -> createAbonnement  (ADMIN)
    // PUT    /abonnements/{id}   -> updateAbonnement  (ADMIN)
    // DELETE /abonnements/{id}   -> deleteAbonnement  (ADMIN)
    // ---------------------------------------------------------
    private Reponse routesAbonnement(String methode, String[] segments, HttpExchange exchange)
            throws IOException, SQLException {
        UtilisateurResponse connecte = getMe(exchange);

        // Un adherent peut seulement lire la liste de ses propres abonnements
        if (segments.length == 2 && methode.equals("GET") && connecte.role().equals("ADHERENT")) {
            return getMesAbonnements(connecte);
        }
        exigerRole(connecte, "ADMIN");

        if (segments.length == 2) {
            switch (methode) {
                case "GET":  return getAbonnements();
                case "POST": return createAbonnement(exchange);
                default:
                    throw new IllegalArgumentException("Methode non supportee : " + methode + " /abonnements");
            }
        }

        if (segments.length == 3) {
            int id = entier(segments[2]);
            switch (methode) {
                case "GET":    return getAbonnement(id);
                case "PUT":    return updateAbonnement(exchange, id);
                case "DELETE": return deleteAbonnement(id);
                default:
                    throw new IllegalArgumentException("Methode non supportee : " + methode + " /abonnements/{id}");
            }
        }

        throw new ResourceNotFoundException("Route inconnue : " + String.join("/", segments));
    }

    private Reponse getAbonnements() throws SQLException {
        return new Reponse(200, gson.toJson(abonnementController.getAbonnements()));
    }

    private Reponse getMesAbonnements(UtilisateurResponse connecte) throws SQLException {
        return new Reponse(200, gson.toJson(abonnementController.getMesAbonnements(connecte.id())));
    }

    private Reponse getAbonnement(int id) throws SQLException {
        return new Reponse(200, gson.toJson(abonnementController.getAbonnement(id)));
    }

    private Reponse createAbonnement(HttpExchange exchange) throws IOException, SQLException {
        AbonnementRequest request = lireCorps(exchange, AbonnementRequest.class);
        return new Reponse(201, gson.toJson(abonnementController.createAbonnement(request)));
    }

    private Reponse updateAbonnement(HttpExchange exchange, int id) throws IOException, SQLException {
        AbonnementRequest request = lireCorps(exchange, AbonnementRequest.class);
        return new Reponse(200, gson.toJson(abonnementController.updateAbonnement(id, request)));
    }

    private Reponse deleteAbonnement(int id) throws SQLException {
        abonnementController.deleteAbonnement(id);
        return new Reponse(204, "");
    }

    // GET /adherents (ADMIN) -> getAdherents : liste pour le formulaire d'abonnement
    private Reponse routesAdherent(String methode, String[] segments, HttpExchange exchange)
            throws SQLException {
        exigerRole(getMe(exchange), "ADMIN");
        if (segments.length == 2 && methode.equals("GET")) {
            return getAdherents();
        }
        throw new ResourceNotFoundException("Route inconnue : " + methode + " " + String.join("/", segments));
    }

    private Reponse getAdherents() throws SQLException {
        return new Reponse(200, gson.toJson(abonnementController.getAdherents()));
    }

    // ---------------------------------------------------------
    // Coaching prive (seances individuelles)
    // GET    /seances              -> getSeances    (ADMIN : toutes, COACH / ADHERENT : les siennes)
    // GET    /seances/{id}         -> getSeance     (son coach, son adherent, ADMIN)
    // POST   /seances              -> createSeance  (ADHERENT pour lui-meme, ADMIN pour un adherent)
    // PUT    /seances/{id}/statut  -> changerStatut (COACH : confirmer / annuler, ADHERENT : annuler)
    // DELETE /seances/{id}         -> deleteSeance  (ADMIN)
    // Les droits fins sont verifies dans SeanceService.
    // ---------------------------------------------------------
    private Reponse routesSeance(String methode, String[] segments, HttpExchange exchange)
            throws IOException, SQLException {
        UtilisateurResponse connecte = getMe(exchange);

        if (segments.length == 2) {
            switch (methode) {
                case "GET":  return getSeances(connecte);
                case "POST": return createSeance(exchange, connecte);
                default:
                    throw new IllegalArgumentException("Methode non supportee : " + methode + " /seances");
            }
        }

        if (segments.length == 3) {
            int id = entier(segments[2]);
            switch (methode) {
                case "GET":    return getSeance(id, connecte);
                case "DELETE": return deleteSeance(id, connecte);
                default:
                    throw new IllegalArgumentException("Methode non supportee : " + methode + " /seances/{id}");
            }
        }

        if (segments.length == 4 && segments[3].equals("statut") && methode.equals("PUT")) {
            return changerStatut(exchange, entier(segments[2]), connecte);
        }

        throw new ResourceNotFoundException("Route inconnue : " + String.join("/", segments));
    }

    private Reponse getSeances(UtilisateurResponse connecte) throws SQLException {
        return new Reponse(200, gson.toJson(seanceController.getSeances(connecte)));
    }

    private Reponse getSeance(int id, UtilisateurResponse connecte) throws SQLException {
        return new Reponse(200, gson.toJson(seanceController.getSeance(id, connecte)));
    }

    private Reponse createSeance(HttpExchange exchange, UtilisateurResponse connecte) throws IOException, SQLException {
        SeanceRequest request = lireCorps(exchange, SeanceRequest.class);
        return new Reponse(201, gson.toJson(seanceController.createSeance(request, connecte)));
    }

    private Reponse changerStatut(HttpExchange exchange, int id, UtilisateurResponse connecte)
            throws IOException, SQLException {
        StatutRequest request = lireCorps(exchange, StatutRequest.class);
        return new Reponse(200, gson.toJson(seanceController.changerStatut(id, request.statut(), connecte)));
    }

    private Reponse deleteSeance(int id, UtilisateurResponse connecte) throws SQLException {
        exigerRole(connecte, "ADMIN");
        seanceController.deleteSeance(id);
        return new Reponse(204, "");
    }

    // ---------------------------------------------------------
    // Outils
    // ---------------------------------------------------------

    // Lit l'en-tete "Authorization: Bearer <token>"
    private String token(HttpExchange exchange) {
        String entete = exchange.getRequestHeaders().getFirst("Authorization");
        if (entete == null || !entete.startsWith("Bearer ")) {
            return null;
        }
        return entete.substring("Bearer ".length()).trim();
    }

    private void exigerRole(UtilisateurResponse utilisateur, String role) {
        if (!utilisateur.role().equals(role)) {
            throw new ForbiddenException("Action reservee au role " + role + ".");
        }
    }

    // Lit tout le corps JSON et le convertit en objet Java (DTO)
    private <T> T lireCorps(HttpExchange exchange, Class<T> type) throws IOException {
        String corps;
        try (InputStream is = exchange.getRequestBody()) {
            corps = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
        if (corps.isBlank()) {
            throw new IllegalArgumentException("Corps de la requete vide.");
        }
        T objet = gson.fromJson(corps, type);
        if (objet == null) {
            throw new IllegalArgumentException("Corps de la requete invalide.");
        }
        return objet;
    }

    private int entier(String valeur) {
        try {
            return Integer.parseInt(valeur);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Identifiant invalide : " + valeur);
        }
    }

    // {"erreur": "..."} construit avec Gson (echappe correctement les guillemets)
    private String messageErreur(String message) {
        JsonObject json = new JsonObject();
        json.addProperty("erreur", message == null ? "Erreur." : message);
        return gson.toJson(json);
    }

    // Autorise le frontend React (autre port) a appeler l'API
    private void ajouterEntetesCors(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    // Envoie la reponse HTTP avec statut et corps JSON
    private void envoyer(HttpExchange exchange, int statut, String corps) throws IOException {
        byte[] bytes = corps == null ? new byte[0] : corps.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > 0) {
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(statut, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        } else {
            // Reponse sans corps (ex. 204 No Content)
            exchange.sendResponseHeaders(statut, -1);
            exchange.close();
        }
    }
}
