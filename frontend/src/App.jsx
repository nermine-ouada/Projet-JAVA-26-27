import { Routes, Route, Navigate } from 'react-router-dom'
import { useAuth } from './AuthContext'
import Layout from './components/Layout'
import Accueil from './pages/Accueil'
import Login from './pages/Login'
import Profil from './pages/Profil'
import Dashboard from './pages/Dashboard'
import Personnes from './pages/Personnes'
// [eya] salles, cours
import Salles from './pages/Salles'
import Planning from './pages/Planning'

// [nermine] abonnements
import Abonnements from './pages/Abonnements'
import Seances from './pages/Seances'

// Bloque l'acces aux pages si l'utilisateur n'est pas connecte
function Protege({ children }) {
  const { utilisateur } = useAuth()
  return utilisateur ? children : <Navigate to="/login" replace />
}

// Page reservee a l'admin (les autres sont renvoyes vers leur profil)
function AdminSeulement({ children }) {
  const { estAdmin } = useAuth()
  return estAdmin ? children : <Navigate to="/profil" replace />
}

// Page d'arrivee apres la connexion, selon le role
function Espace() {
  const { estAdmin } = useAuth()
  return <Navigate to={estAdmin ? '/tableau-de-bord' : '/profil'} replace />
}

export default function App() {
  return (
    <Routes>
      {/* Pages publiques */}
      <Route path="/" element={<Accueil />} />
      <Route path="/login" element={<Login />} />

      {/* Espace connecte (menu lateral + page) */}
      <Route element={<Protege><Layout /></Protege>}>
        <Route path="/espace" element={<Espace />} />
        <Route path="/profil" element={<Profil />} />
        <Route path="/tableau-de-bord" element={<AdminSeulement><Dashboard /></AdminSeulement>} />
        <Route path="/adherents" element={<AdminSeulement><Personnes role="ADHERENT" /></AdminSeulement>} />
        <Route path="/coachs" element={<AdminSeulement><Personnes role="COACH" /></AdminSeulement>} />
        {/* [eya] salles, cours */}
        <Route path="/planning" element={<Planning />} />
        <Route path="/salles" element={<Salles />} />

        {/* [nermine] abonnements */}
        <Route path="/abonnements" element={<AdminSeulement><Abonnements /></AdminSeulement>} />
        <Route path="/seances" element={<Seances />} />

      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
