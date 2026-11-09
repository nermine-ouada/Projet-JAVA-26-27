# Frontend : gestion salle de sport

Interface web (React + Vite) qui consomme l'API Java du dossier `backend-java`.

## Prérequis

- Node.js 18 ou plus
- Le backend démarré sur `http://localhost:8081` (MySQL lancé + `Main.java` exécuté)

## Lancer

```bash
npm install     # une seule fois
npm run dev     # http://localhost:5173 (ou 5174 si le port est pris)
```

L'adresse de l'API se change dans `src/api.js` (`API_URL`).

## Comptes de test

| Rôle     | Email                  | Mot de passe |
|----------|------------------------|--------------|
| Admin    | admin@fitness.tn       | admin123     |
| Coach    | sami.coach@fitness.tn  | coach123     |
| Adhérent | yassine@mail.tn        | pass123      |

Seul l'admin voit la page **Utilisateurs** (CRUD : créer, consulter, modifier, supprimer).
Les autres rôles voient seulement **Mon profil**.

## Structure

```
src/
  api.js            appels HTTP vers l'API + stockage du token
  AuthContext.jsx   utilisateur connecté, login / register / logout
  App.jsx           routes (pages publiques et protégées)
  components/       Layout (barre du haut), Modal, AuthShell
  pages/            Login, Register, Profil, Utilisateurs (CRUD utilisateurs, admin)
  styles.css        tout le style (variables CSS en haut du fichier)
```
