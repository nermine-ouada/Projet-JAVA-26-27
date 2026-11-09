import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom'
// Icones du menu : chaque branche importe ici celles dont elle a besoin
import { Dumbbell, LogOut, UserRound, Users, CalendarDays, Building2, Ticket } from 'lucide-react'
import { useAuth } from '../AuthContext'

const ROLES = { ADMIN: 'Admin', COACH: 'Coach', ADHERENT: 'Adhérent' }

// Menu lateral (tableau de bord) + contenu de la page courante (<Outlet />)
export default function Layout() {
  const { utilisateur, estAdmin, logout } = useAuth()
  const navigate = useNavigate()

  async function deconnexion() {
    await logout()
    navigate('/')
  }

  return (
    <div className="dashboard">
      <aside className="sidebar">
        <div className="sidebar-inner">
          <Link to="/" className="brand brand-icon sidebar-brand"><Dumbbell size={20} /> Club Atlas</Link>
          <nav className="sidebar-nav">
            <NavLink to="/profil"><UserRound size={18} /> Mon profil</NavLink>
            {estAdmin && <NavLink to="/utilisateurs"><Users size={18} /> Utilisateurs</NavLink>}
            {/* [eya] salles, cours */}
            <NavLink to="/planning"><CalendarDays size={18} /> Planning</NavLink>
            <NavLink to="/salles"><Building2 size={18} /> Salles</NavLink>

            {/* [nermine] abonnements */}

          </nav>
          <div className="sidebar-user">
            <div className="sidebar-user-info">
              <span className="user-name">{utilisateur.prenom} {utilisateur.nom}</span>
              <span className={'badge badge-' + utilisateur.role.toLowerCase()}>
                {ROLES[utilisateur.role]}
              </span>
            </div>
            <button className="btn btn-ghost sidebar-logout" onClick={deconnexion}>
              <LogOut size={16} /> Déconnexion
            </button>
          </div>
        </div>
      </aside>
      <main className="page">
        <Outlet />
      </main>
    </div>
  )
}
