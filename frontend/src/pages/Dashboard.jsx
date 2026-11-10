import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { Users, Dumbbell, UserPlus, ArrowRight } from 'lucide-react'
import { getUtilisateurs } from '../api'
import { useAuth } from '../AuthContext'
import Avatar from '../components/Avatar'
import Loader from '../components/Loader'
// [eya] tuiles

// [nermine] tuiles


// Tableau de bord de l'admin : chiffres cles + derniers inscrits
export default function Dashboard() {
  const { utilisateur } = useAuth()
  const [utilisateurs, setUtilisateurs] = useState(null)
  const [erreur, setErreur] = useState('')

  useEffect(() => {
    getUtilisateurs().then(setUtilisateurs).catch((err) => setErreur(err.message))
  }, [])

  const adherents = (utilisateurs || []).filter((u) => u.role === 'ADHERENT')
  const coachs = (utilisateurs || []).filter((u) => u.role === 'COACH')
  // Les id sont croissants : les plus grands sont les derniers comptes crees
  const derniers = [...adherents].sort((a, b) => b.id - a.id).slice(0, 5)

  const aujourdhui = new Date().toLocaleDateString('fr-FR', { weekday: 'long', day: 'numeric', month: 'long' })

  return (
    <>
      <div className="page-head">
        <div>
          <h1>Bonjour {utilisateur.prenom}</h1>
          <p className="muted" style={{ textTransform: 'capitalize' }}>{aujourdhui}</p>
        </div>
      </div>

      {erreur && <p className="error">{erreur}</p>}
      {!utilisateurs && !erreur && <Loader />}

      {utilisateurs && (
        <>
          <div className="tiles stagger">
            <Link to="/adherents" className="tile tile-hot" style={{ '--i': 0 }}>
              <span className="tile-label"><Users size={16} aria-hidden="true" /> Adhérents</span>
              <span className="tile-value">{adherents.length}</span>
              <span className="tile-sub">comptes actifs</span>
            </Link>
            <Link to="/coachs" className="tile" style={{ '--i': 1 }}>
              <span className="tile-label"><Dumbbell size={16} aria-hidden="true" /> Coachs</span>
              <span className="tile-value">{coachs.length}</span>
              <span className="tile-sub">dans l'équipe</span>
            </Link>
            {/* [eya] tuiles */}

            {/* [nermine] tuiles */}

          </div>

          <h2 className="section-sous-titre"><UserPlus size={18} aria-hidden="true" /> Derniers adhérents</h2>
          <div className="card stagger">
            {derniers.length === 0 && <p className="muted" style={{ padding: 16 }}>Aucun adhérent pour le moment.</p>}
            {derniers.map((a, i) => (
              <div key={a.id} className="ligne-personne" style={{ '--i': i }}>
                <Avatar personne={a} taille="sm" />
                <div>
                  <div className="strong">{a.prenom} {a.nom}</div>
                  <div className="hint">{a.email}</div>
                </div>
              </div>
            ))}
            <Link to="/adherents" className="ligne-lien">Voir tous les adhérents <ArrowRight size={14} aria-hidden="true" /></Link>
          </div>
        </>
      )}
    </>
  )
}
