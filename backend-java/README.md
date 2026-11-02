# Backend — Gestion de salle de sport

API REST en Java pur (sans framework), selon le Guide :

```
Main -> ApiServer -> Router -> Controller -> Service -> DAO -> DatabaseConnection -> MySQL
                       |
                 DTO <-> Gson (JSON) <-> Postman / React
```

## Lancer le projet

1. Demarrer **MySQL** dans XAMPP.
2. Creer la base : executer `src/main/resources/database/database.sql`
   (phpMyAdmin > Importer, ou lancer `config/DatabaseConnection.main()` dans IntelliJ).
   Attention : le script commence par `DROP DATABASE gestion_salle_sport`.
3. Lancer `tn.enicarthage.gestionSalleDeSport.Main` (ou `mvn compile exec:java`).
4. API disponible sur `http://localhost:8081`.

> Port 8081 et non 8080 : sur certains PC, 8080 est deja pris par Oracle Database
> (une fenetre de connexion "XDB" s'ouvre dans le navigateur). Le port se change dans
> `Main.java` (constante `PORT`) et dans `frontend/src/api.js` (`API_URL`).

## Comptes de test

| Role     | Email                    | Mot de passe |
|----------|--------------------------|--------------|
| ADMIN    | admin@fitness.tn         | admin123     |
| COACH    | sami.coach@fitness.tn    | coach123     |
| ADHERENT | yassine@mail.tn          | pass123      |

Les mots de passe sont stockes hashes (`sel:sha256(sel + mdp)`), voir `util/PasswordUtil.java`.

## Authentification

1. `POST /auth/login` renvoie un `token`.
2. Pour les autres routes, ajouter l'en-tete : `Authorization: Bearer <token>`.
3. Le token est stocke dans la table `session` et expire apres 7 jours.

Le role (ADMIN / COACH / ADHERENT) est deduit de la table qui contient l'utilisateur
(`administrateur`, `coach` ou `adherent`).

## Routes

| Methode | URL                 | Acces    | Corps JSON |
|---------|---------------------|----------|------------|
| POST    | /auth/register      | public   | `{"nom":"Ali","prenom":"Sara","email":"sara@mail.tn","motDePasse":"secret1"}` |
| POST    | /auth/login         | public   | `{"email":"admin@fitness.tn","motDePasse":"admin123"}` |
| GET     | /auth/me            | connecte | — |
| POST    | /auth/logout        | connecte | — |
| GET     | /utilisateurs       | ADMIN    | — |
| GET     | /utilisateurs/{id}  | ADMIN    | — |
| POST    | /utilisateurs       | ADMIN    | `{"nom":"Ali","prenom":"Sara","email":"sara@mail.tn","motDePasse":"secret1","role":"COACH"}` |
| PUT     | /utilisateurs/{id}  | ADMIN    | `{"nom":"Ali","prenom":"Sara","email":"sara@mail.tn","motDePasse":""}` |
| DELETE  | /utilisateurs/{id}  | ADMIN    | — |

- `role` : `ADMIN`, `COACH` ou `ADHERENT` (fixe a la creation, ignore en modification).
- En modification, `motDePasse` vide = mot de passe inchange.
- L'admin ne peut pas supprimer son propre compte.

## Codes HTTP

| Code | Cas |
|------|-----|
| 200 / 201 / 204 | OK / cree / supprime |
| 400 | donnees invalides (`{"erreur":"..."}`) |
| 401 | pas de token, token invalide ou expire |
| 403 | role insuffisant (ex. adherent qui appelle /utilisateurs) |
| 404 | ressource introuvable |
| 409 | email deja utilise, ou utilisateur encore reference (ex. coach qui anime des cours) |
| 500 | erreur serveur |
