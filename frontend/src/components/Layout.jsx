import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../AuthContext'

const ROLES = { ADMIN: 'Admin', COACH: 'Coach', ADHERENT: 'Adhérent' }

// Barre du haut + contenu de la page courante (<Outlet />)
export default function Layout() {
  const { utilisateur, estAdmin, logout } = useAuth()
  const navigate = useNavigate()

  async function deconnexion() {
    await logout()
    navigate('/login')
  }

  return (
    <>
      <header className="topbar">
        <div className="topbar-inner">
          <span className="brand">Club Atlas</span>
          <nav className="nav">
            <NavLink to="/" end>Mon profil</NavLink>
            {estAdmin && <NavLink to="/utilisateurs">Utilisateurs</NavLink>}
          </nav>
          <div className="user">
            <span className="user-name">{utilisateur.prenom} {utilisateur.nom}</span>
            <span className={'badge badge-' + utilisateur.role.toLowerCase()}>
              {ROLES[utilisateur.role]}
            </span>
            <button className="btn btn-ghost" onClick={deconnexion}>Déconnexion</button>
          </div>
        </div>
      </header>
      <main className="page">
        <Outlet />
      </main>
    </>
  )
}
