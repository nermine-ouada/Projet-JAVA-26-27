# Tests Postman

Une collection par module, pour que chaque branche ajoute son propre fichier sans conflit :

| Fichier | Module | Branche |
|---------|--------|---------|
| `01-auth-utilisateurs.postman_collection.json` | connexion, inscription, CRUD utilisateurs | main |
| `02-salles-cours.postman_collection.json` | CRUD salles et cours, liste des coachs | eya |
| `03-abonnements.postman_collection.json` | CRUD abonnements, liste des adherents | nermine |
| `local.postman_environment.json` | variable `baseUrl` = `http://localhost:8081` | main |

## Dans Postman

1. Demarrer MySQL et le backend (`Main`).
2. **Import** : glisser les fichiers de ce dossier dans Postman.
3. En haut a droite, choisir l'environnement **Local (backend sur 8081)**.
4. Clic droit sur une collection > **Run collection** > **Run**.
   Les requetes doivent etre lancees dans l'ordre : le login enregistre les tokens,
   les creations enregistrent les id utilises par les requetes suivantes.

Chaque requete contient des tests (onglet **Tests**) : code HTTP attendu, message d'erreur,
valeurs renvoyees. Chaque collection supprime ce qu'elle cree, la base reste propre.

## En ligne de commande (newman)

```bash
npx newman run postman/01-auth-utilisateurs.postman_collection.json -e postman/local.postman_environment.json
```
