# Backend : gestion de salle de sport

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
   Base deja creee ? Executer plutot les fichiers de `src/main/resources/database/migrations/`
   (ajouts sans perte de donnees).
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
| GET     | /auth/me            | connecte | aucun |
| POST    | /auth/logout        | connecte | aucun |
| PUT     | /auth/photo         | connecte | `{"photo":"data:image/jpeg;base64,..."}` ou `{"photo":""}` pour la retirer |
| GET     | /utilisateurs       | ADMIN    | aucun |
| GET     | /utilisateurs/{id}  | ADMIN    | aucun |
| POST    | /utilisateurs       | ADMIN    | `{"nom":"Ali","prenom":"Sara","email":"sara@mail.tn","motDePasse":"secret1","role":"COACH","telephone":"22000000","specialite":"Boxe","photo":"data:image/..."}` |
| PUT     | /utilisateurs/{id}  | ADMIN    | `{"nom":"Ali","prenom":"Sara","email":"sara@mail.tn","motDePasse":"","telephone":"22000000","specialite":"Boxe"}` |
| DELETE  | /utilisateurs/{id}  | ADMIN    | aucun |
| GET     | /coachs             | connecte | aucun (id de la table coach, nom, specialite, photo) |

- `role` : `ADMIN`, `COACH` ou `ADHERENT` (fixe a la creation, ignore en modification).
- `telephone`, `specialite` (coach) et `photo` sont facultatifs.
- En modification, `motDePasse` vide = mot de passe inchange ; `photo` absente = inchangee, `""` = retiree.
- La photo est une image en base64 ; le frontend la reduit a 256 x 256 px avant l'envoi (limite : ~500 Ko).
- L'admin ne peut pas supprimer son propre compte.
- Un champ vide est renvoye en `null` (ex. `"photo": null`).

<!-- [eya] salles, cours -->

<!-- [nermine] abonnements -->

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
