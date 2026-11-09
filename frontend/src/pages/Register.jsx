import { useState } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { useAuth } from '../AuthContext'
import AuthShell from '../components/AuthShell'

export default function Register() {
  const { utilisateur, register } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({ nom: '', prenom: '', email: '', motDePasse: '' })
  const [erreur, setErreur] = useState('')
  const [chargement, setChargement] = useState(false)

  if (utilisateur) return <Navigate to="/profil" replace />

  // Met a jour un champ du formulaire a partir de son attribut "name"
  function changer(e) {
    setForm({ ...form, [e.target.name]: e.target.value })
  }

  async function envoyer(e) {
    e.preventDefault()
    setErreur('')
    setChargement(true)
    try {
      await register(form)
      navigate('/profil')
    } catch (err) {
      setErreur(err.message)
    } finally {
      setChargement(false)
    }
  }

  return (
    <AuthShell>
      <h1>Inscription</h1>
      <p className="muted">Créez votre compte adhérent.</p>
      <form onSubmit={envoyer} className="form">
        <div className="row">
          <label>
            Prénom
            <input name="prenom" value={form.prenom} onChange={changer} required />
          </label>
          <label>
            Nom
            <input name="nom" value={form.nom} onChange={changer} required />
          </label>
        </div>
        <label>
          Email
          <input name="email" type="email" value={form.email} onChange={changer} required />
        </label>
        <label>
          Mot de passe <span className="hint">6 caractères minimum</span>
          <input name="motDePasse" type="password" minLength={6} value={form.motDePasse} onChange={changer} required />
        </label>
        {erreur && <p className="error">{erreur}</p>}
        <button className="btn btn-primary btn-block" disabled={chargement}>
          {chargement ? 'Création…' : 'Créer mon compte'}
        </button>
      </form>
      <p className="muted small">
        Déjà inscrit ? <Link to="/login">Se connecter</Link>
      </p>
    </AuthShell>
  )
}
