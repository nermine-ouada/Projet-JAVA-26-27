import { Routes, Route, Navigate } from 'react-router-dom'
import { useAuth } from './AuthContext'
import Layout from './components/Layout'
import Accueil from './pages/Accueil'
import Login from './pages/Login'
import Register from './pages/Register'
import Profil from './pages/Profil'
import Utilisateurs from './pages/Utilisateurs'
// [eya] salles, cours
import Salles from './pages/Salles'
import Planning from './pages/Planning'

// [nermine] abonnements

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

export default function App() {
  return (
    <Routes>
      {/* Pages publiques */}
      <Route path="/" element={<Accueil />} />
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />

      {/* Espace connecte (barre du haut + page) */}
      <Route element={<Protege><Layout /></Protege>}>
        <Route path="/profil" element={<Profil />} />
        <Route path="/utilisateurs" element={<AdminSeulement><Utilisateurs /></AdminSeulement>} />
        {/* [eya] salles, cours */}
        <Route path="/planning" element={<Planning />} />
        <Route path="/salles" element={<Salles />} />

        {/* [nermine] abonnements */}

      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
