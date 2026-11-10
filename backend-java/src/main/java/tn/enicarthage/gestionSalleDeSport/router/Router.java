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
    private final SalleController salleController = new SalleController();
    private final CoursController coursController = new CoursController();
    private final InscriptionController inscriptionController = new InscriptionController();

    // [nermine] abonnements


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
            case "salles":
                return routesSalle(methode, segments, exchange);
            case "cours":
                return routesCours(methode, segments, exchange);

            // [nermine] abonnements

            default:
                throw new ResourceNotFoundException("Route inconnue : /" + segments[1]);
        }
    }

    // ---------------------------------------------------------
    // Authentification
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
            case "POST login":    return login(exchange);
            case "POST logout":   return logout(exchange);
            case "GET me":        return new Reponse(200, gson.toJson(getMe(exchange)));
            case "PUT photo":     return updateMaPhoto(exchange);
            default:
                throw new ResourceNotFoundException("Route inconnue : " + methode + " /auth/" + segments[2]);
        }
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

    // ---------------------------------------------------------
    // CRUD Salles (lecture : connecte, ecriture : ADMIN)
    // GET    /salles        -> getSalles
    // GET    /salles/{id}   -> getSalle
    // POST   /salles        -> createSalle
    // PUT    /salles/{id}   -> updateSalle
    // DELETE /salles/{id}   -> deleteSalle
    // ---------------------------------------------------------
    private Reponse routesSalle(String methode, String[] segments, HttpExchange exchange)
            throws IOException, SQLException {
        UtilisateurResponse connecte = getMe(exchange);
        if (!methode.equals("GET")) {
            exigerRole(connecte, "ADMIN");
        }

        if (segments.length == 2) {
            switch (methode) {
                case "GET":  return getSalles();
                case "POST": return createSalle(exchange);
                default:
                    throw new IllegalArgumentException("Methode non supportee : " + methode + " /salles");
            }
        }

        if (segments.length == 3) {
            int id = entier(segments[2]);
            switch (methode) {
                case "GET":    return getSalle(id);
                case "PUT":    return updateSalle(exchange, id);
                case "DELETE": return deleteSalle(id);
                default:
                    throw new IllegalArgumentException("Methode non supportee : " + methode + " /salles/{id}");
            }
        }

        throw new ResourceNotFoundException("Route inconnue : " + String.join("/", segments));
    }

    private Reponse getSalles() throws SQLException {
        return new Reponse(200, gson.toJson(salleController.getSalles()));
    }

    private Reponse getSalle(int id) throws SQLException {
        return new Reponse(200, gson.toJson(salleController.getSalle(id)));
    }

    private Reponse createSalle(HttpExchange exchange) throws IOException, SQLException {
        SalleRequest request = lireCorps(exchange, SalleRequest.class);
        return new Reponse(201, gson.toJson(salleController.createSalle(request)));
    }

    private Reponse updateSalle(HttpExchange exchange, int id) throws IOException, SQLException {
        SalleRequest request = lireCorps(exchange, SalleRequest.class);
        return new Reponse(200, gson.toJson(salleController.updateSalle(id, request)));
    }

    private Reponse deleteSalle(int id) throws SQLException {
        salleController.deleteSalle(id);
        return new Reponse(204, "");
    }

    // ---------------------------------------------------------
    // CRUD Cours / planning (lecture : connecte, ecriture : ADMIN)
    // GET    /cours        -> getAllCours
    // GET    /cours/{id}   -> getCours
    // POST   /cours        -> createCours
    // PUT    /cours/{id}   -> updateCours
    // DELETE /cours/{id}   -> deleteCours
    //
    // Inscriptions
    // POST   /cours/{id}/inscription   -> inscrire     (ADHERENT)
    // DELETE /cours/{id}/inscription   -> desinscrire  (ADHERENT)
    // GET    /cours/{id}/participants  -> getParticipants (ADMIN, COACH)
    // ---------------------------------------------------------
    private Reponse routesCours(String methode, String[] segments, HttpExchange exchange)
            throws IOException, SQLException {
        UtilisateurResponse connecte = getMe(exchange);

        // Sous-routes /cours/{id}/... : chacune verifie son propre role
        if (segments.length == 4) {
            int id = entier(segments[2]);
            switch (methode + " " + segments[3]) {
                case "POST inscription":   return inscrire(id, connecte);
                case "DELETE inscription": return desinscrire(id, connecte);
                case "GET participants":   return getParticipants(id, connecte);
                default:
                    throw new ResourceNotFoundException("Route inconnue : " + methode + " " + String.join("/", segments));
            }
        }

        if (!methode.equals("GET")) {
            exigerRole(connecte, "ADMIN");
        }

        if (segments.length == 2) {
            switch (methode) {
                case "GET":  return getAllCours(connecte);
                case "POST": return createCours(exchange);
                default:
                    throw new IllegalArgumentException("Methode non supportee : " + methode + " /cours");
            }
        }

        if (segments.length == 3) {
            int id = entier(segments[2]);
            switch (methode) {
                case "GET":    return getCours(id, connecte);
                case "PUT":    return updateCours(exchange, id);
                case "DELETE": return deleteCours(id);
                default:
                    throw new IllegalArgumentException("Methode non supportee : " + methode + " /cours/{id}");
            }
        }

        throw new ResourceNotFoundException("Route inconnue : " + String.join("/", segments));
    }

    private Reponse getAllCours(UtilisateurResponse connecte) throws SQLException {
        return new Reponse(200, gson.toJson(coursController.getAllCours(connecte.id())));
    }

    private Reponse getCours(int id, UtilisateurResponse connecte) throws SQLException {
        return new Reponse(200, gson.toJson(coursController.getCours(id, connecte.id())));
    }

    private Reponse inscrire(int coursId, UtilisateurResponse connecte) throws SQLException {
        return new Reponse(201, gson.toJson(inscriptionController.inscrire(coursId, connecte.id())));
    }

    private Reponse desinscrire(int coursId, UtilisateurResponse connecte) throws SQLException {
        inscriptionController.desinscrire(coursId, connecte.id());
        return new Reponse(204, "");
    }

    private Reponse getParticipants(int coursId, UtilisateurResponse connecte) throws SQLException {
        if (connecte.role().equals("ADHERENT")) {
            throw new ForbiddenException("Liste reservee a l'admin et aux coachs.");
        }
        return new Reponse(200, gson.toJson(inscriptionController.getParticipants(coursId)));
    }

    private Reponse createCours(HttpExchange exchange) throws IOException, SQLException {
        CoursRequest request = lireCorps(exchange, CoursRequest.class);
        return new Reponse(201, gson.toJson(coursController.createCours(request)));
    }

    private Reponse updateCours(HttpExchange exchange, int id) throws IOException, SQLException {
        CoursRequest request = lireCorps(exchange, CoursRequest.class);
        return new Reponse(200, gson.toJson(coursController.updateCours(id, request)));
    }

    private Reponse deleteCours(int id) throws SQLException {
        coursController.deleteCours(id);
        return new Reponse(204, "");
    }

    // =========================================================
    // [nermine] Routes abonnements

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
