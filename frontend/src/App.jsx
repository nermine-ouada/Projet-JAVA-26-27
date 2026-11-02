import { Routes, Route, Navigate } from 'react-router-dom'
import { useAuth } from './AuthContext'
import Layout from './components/Layout'
import Login from './pages/Login'
import Register from './pages/Register'
import Profil from './pages/Profil'
import Utilisateurs from './pages/Utilisateurs'

// Bloque l'acces aux pages si l'utilisateur n'est pas connecte
function Protege({ children }) {
  const { utilisateur } = useAuth()
  return utilisateur ? children : <Navigate to="/login" replace />
}

// Page reservee a l'admin (les autres sont renvoyes vers leur profil)
function AdminSeulement({ children }) {
  const { estAdmin } = useAuth()
  return estAdmin ? children : <Navigate to="/" replace />
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route element={<Protege><Layout /></Protege>}>
        <Route path="/" element={<Profil />} />
        <Route path="/utilisateurs" element={<AdminSeulement><Utilisateurs /></AdminSeulement>} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
