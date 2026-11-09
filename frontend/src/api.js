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
export const register = (utilisateur) => request('/auth/register', 'POST', utilisateur)
export const logout = () => request('/auth/logout', 'POST')
export const getMe = () => request('/auth/me')

// ---------- Utilisateurs (CRUD, admin) ----------

export const getUtilisateurs = () => request('/utilisateurs')
export const getUtilisateur = (id) => request('/utilisateurs/' + id)
export const createUtilisateur = (utilisateur) => request('/utilisateurs', 'POST', utilisateur)
export const updateUtilisateur = (id, utilisateur) => request('/utilisateurs/' + id, 'PUT', utilisateur)
export const deleteUtilisateur = (id) => request('/utilisateurs/' + id, 'DELETE')

// ---------- [eya] Salles, Cours ----------

// ---------- [nermine] Abonnements ----------

// Admin : tous les abonnements. Adherent : seulement les siens.
export const getAbonnements = () => request('/abonnements')
export const getAbonnement = (id) => request('/abonnements/' + id)
export const createAbonnement = (abonnement) => request('/abonnements', 'POST', abonnement)
export const updateAbonnement = (id, abonnement) => request('/abonnements/' + id, 'PUT', abonnement)
export const deleteAbonnement = (id) => request('/abonnements/' + id, 'DELETE')

export const getAdherents = () => request('/adherents')
