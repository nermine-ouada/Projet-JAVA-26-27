import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom'
import { Dumbbell, LogOut } from 'lucide-react'
import { useAuth } from '../AuthContext'

const ROLES = { ADMIN: 'Admin', COACH: 'Coach', ADHERENT: 'Adhérent' }

// Barre du haut + contenu de la page courante (<Outlet />)
export default function Layout() {
  const { utilisateur, estAdmin, logout } = useAuth()
  const navigate = useNavigate()

  async function deconnexion() {
    await logout()
    navigate('/')
  }

  return (
    <>
      <header className="topbar">
        <div className="topbar-inner">
          <Link to="/" className="brand brand-icon"><Dumbbell size={20} /> Club Atlas</Link>
          <nav className="nav">
            <NavLink to="/profil">Mon profil</NavLink>
            {estAdmin && <NavLink to="/utilisateurs">Utilisateurs</NavLink>}
            {/* [eya] salles, cours */}
            <NavLink to="/planning">Planning</NavLink>
            <NavLink to="/salles">Salles</NavLink>

            {/* [nermine] abonnements */}

          </nav>
          <div className="user">
            <span className="user-name">{utilisateur.prenom} {utilisateur.nom}</span>
            <span className={'badge badge-' + utilisateur.role.toLowerCase()}>
              {ROLES[utilisateur.role]}
            </span>
            <button className="btn btn-ghost" onClick={deconnexion}><LogOut size={16} /> Déconnexion</button>
          </div>
        </div>
      </header>
      <main className="page">
        <Outlet />
      </main>
    </>
  )
}
