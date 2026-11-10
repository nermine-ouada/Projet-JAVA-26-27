import { createContext, useContext, useState } from 'react'
import * as api from './api'

const AuthContext = createContext(null)

// Fournit l'utilisateur connecte + login / logout a toute l'application
export function AuthProvider({ children }) {
  const [utilisateur, setUtilisateur] = useState(api.getSavedUser())

  async function login(email, motDePasse) {
    const res = await api.login(email, motDePasse)
    api.saveSession(res.token, res.utilisateur)
    setUtilisateur(res.utilisateur)
  }

  async function logout() {
    try {
      await api.logout()
    } catch {
      // meme si le serveur ne repond pas, on deconnecte cote client
    }
    api.clearSession()
    setUtilisateur(null)
  }

  // Met a jour l'utilisateur garde en memoire (ex. apres un changement de photo)
  function majUtilisateur(nouveau) {
    api.saveSession(localStorage.getItem('token'), nouveau)
    setUtilisateur(nouveau)
  }

  const estAdmin = utilisateur?.role === 'ADMIN'
  const estCoach = utilisateur?.role === 'COACH'
  const estAdherent = utilisateur?.role === 'ADHERENT'

  return (
    <AuthContext.Provider value={{ utilisateur, estAdmin, estCoach, estAdherent, login, logout, majUtilisateur }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  return useContext(AuthContext)
}
