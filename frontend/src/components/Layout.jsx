import { Link, NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom'
// Icones du menu : chaque branche importe ici celles dont elle a besoin
import {
  Dumbbell, LogOut, UserRound, Users, LayoutDashboard,
  CalendarDays, Building2, Ticket, Handshake,
} from 'lucide-react'
import { useAuth } from '../AuthContext'
import { ROLES } from '../roles'
import Avatar from './Avatar'
import ThemeToggle from './ThemeToggle'

// Menu lateral (tableau de bord) + contenu de la page courante (<Outlet />).
// Le menu change selon le role ; "Mon profil" est toujours en dernier.
export default function Layout() {
  const { utilisateur, estAdmin, estCoach, estAdherent, logout } = useAuth()
  const navigate = useNavigate()
  const location = useLocation() // la cle ci-dessous rejoue l'animation d'entree a chaque page

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
            {estAdmin && <NavLink to="/tableau-de-bord"><LayoutDashboard size={18} /> Tableau de bord</NavLink>}
            {estAdmin && <NavLink to="/adherents"><Users size={18} /> Adhérents</NavLink>}
            {estAdmin && <NavLink to="/coachs"><Dumbbell size={18} /> Coachs</NavLink>}
            {/* [eya] salles, cours */}

            {/* [nermine] abonnements */}
            {estAdmin && <NavLink to="/abonnements"><Ticket size={18} /> Abonnements</NavLink>}
            <NavLink to="/seances"><Handshake size={18} /> {estAdmin ? 'Séances privées' : estCoach ? 'Mes séances' : 'Coaching privé'}</NavLink>

            <NavLink to="/profil"><UserRound size={18} /> Mon profil</NavLink>
          </nav>
          <div className="sidebar-user">
            <div className="sidebar-user-info">
              <Avatar personne={utilisateur} taille="sm" />
              <div>
                <span className="user-name">{utilisateur.prenom} {utilisateur.nom}</span>
                <span className={'badge badge-' + utilisateur.role.toLowerCase()}>
                  {ROLES[utilisateur.role]}
                </span>
              </div>
            </div>
            <div className="sidebar-actions">
              <ThemeToggle />
              <button className="btn btn-ghost sidebar-logout" onClick={deconnexion}>
                <LogOut size={16} /> Déconnexion
              </button>
            </div>
          </div>
        </div>
      </aside>
      <main className="page" key={location.pathname}>
        <Outlet />
      </main>
    </div>
  )
}
