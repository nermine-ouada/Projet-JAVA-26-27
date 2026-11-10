import { useState } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { useAuth } from '../AuthContext'
import AuthShell from '../components/AuthShell'

export default function Login() {
  const { utilisateur, login } = useAuth()
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [motDePasse, setMotDePasse] = useState('')
  const [erreur, setErreur] = useState('')
  const [chargement, setChargement] = useState(false)

  if (utilisateur) return <Navigate to="/espace" replace />

  async function envoyer(e) {
    e.preventDefault()
    setErreur('')
    setChargement(true)
    try {
      await login(email, motDePasse)
      navigate('/espace')
    } catch (err) {
      setErreur(err.message)
    } finally {
      setChargement(false)
    }
  }

  return (
    <AuthShell>
      <h1>Connexion</h1>
      <p className="muted">Accédez à votre espace.</p>
      <form onSubmit={envoyer} className="form">
        <label>
          Email
          <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required autoFocus />
        </label>
        <label>
          Mot de passe
          <input type="password" value={motDePasse} onChange={(e) => setMotDePasse(e.target.value)} required />
        </label>
        {erreur && <p className="error">{erreur}</p>}
        <button className="btn btn-primary btn-block" disabled={chargement}>
          {chargement ? 'Connexion…' : 'Se connecter'}
        </button>
      </form>
      <p className="muted small">
        Pas encore de compte ? <Link to="/register">Créer un compte</Link>
      </p>
    </AuthShell>
  )
}
