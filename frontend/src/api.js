// Adresse du backend Java
const API_URL = 'http://localhost:8081'

// ---------- Session (token + utilisateur dans le navigateur) ----------

export function saveSession(token, utilisateur) {
  localStorage.setItem('token', token)
  localStorage.setItem('utilisateur', JSON.stringify(utilisateur))
}

export function clearSession() {
  localStorage.removeItem('token')
  localStorage.removeItem('utilisateur')
}

export function getSavedUser() {
  const json = localStorage.getItem('utilisateur')
  return json ? JSON.parse(json) : null
}

// ---------- Fonction de base : envoie la requete et renvoie le JSON ----------

async function request(chemin, methode = 'GET', corps) {
  const token = localStorage.getItem('token')

  let reponse
  try {
    reponse = await fetch(API_URL + chemin, {
      method: methode,
      headers: {
        'Content-Type': 'application/json',
        Authorization: token ? 'Bearer ' + token : '',
      },
      body: corps ? JSON.stringify(corps) : undefined,
    })
  } catch {
    throw new Error('Serveur injoignable. Le backend est-il demarre sur le port 8081 ?')
  }

  const texte = await reponse.text() // vide pour 204 No Content
  const donnees = texte ? JSON.parse(texte) : null

  if (!reponse.ok) {
    // Token invalide ou expire : retour a la page de connexion
    if (reponse.status === 401 && token) {
      clearSession()
      window.location.href = '/login'
    }
    throw new Error(donnees?.erreur || 'Erreur ' + reponse.status)
  }
  return donnees
}

// ---------- Auth ----------

export const login = (email, motDePasse) => request('/auth/login', 'POST', { email, motDePasse })
export const logout = () => request('/auth/logout', 'POST')
export const getMe = () => request('/auth/me')
// Photo de l'utilisateur connecte : image en texte (data URL), ou '' pour la retirer
export const updateMaPhoto = (photo) => request('/auth/photo', 'PUT', { photo })

// ---------- Utilisateurs (CRUD, admin) ----------

export const getUtilisateurs = () => request('/utilisateurs')
export const getUtilisateur = (id) => request('/utilisateurs/' + id)
export const createUtilisateur = (utilisateur) => request('/utilisateurs', 'POST', utilisateur)
export const updateUtilisateur = (id, utilisateur) => request('/utilisateurs/' + id, 'PUT', utilisateur)
export const deleteUtilisateur = (id) => request('/utilisateurs/' + id, 'DELETE')

// ---------- Coachs (liste pour les formulaires) ----------

export const getCoachs = () => request('/coachs')

// ---------- [eya] Salles, Cours ----------

// ---------- [nermine] Abonnements ----------

// Admin : tous les abonnements. Adherent : seulement les siens.
export const getAbonnements = () => request('/abonnements')
export const getAbonnement = (id) => request('/abonnements/' + id)
export const createAbonnement = (abonnement) => request('/abonnements', 'POST', abonnement)
export const updateAbonnement = (id, abonnement) => request('/abonnements/' + id, 'PUT', abonnement)
export const deleteAbonnement = (id) => request('/abonnements/' + id, 'DELETE')

export const getAdherents = () => request('/adherents')

// Coaching prive. Admin : toutes les seances ; coach et adherent : les leurs.
export const getSeances = () => request('/seances')
export const getSeance = (id) => request('/seances/' + id)
export const createSeance = (seance) => request('/seances', 'POST', seance)
// statut : 'CONFIRMEE' (coach) ou 'ANNULEE' (coach ou adherent)
export const changerStatutSeance = (id, statut) => request('/seances/' + id + '/statut', 'PUT', { statut })
export const deleteSeance = (id) => request('/seances/' + id, 'DELETE')
