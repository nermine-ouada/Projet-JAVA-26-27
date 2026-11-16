import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getUtilisateurs } from '../api'
import Avatar from '../components/Avatar'
import Loader from '../components/Loader'
// [eya] tuiles
import TuileCours from '../components/TuileCours'

// [nermine] tuiles
import TuilesNermine from '../components/TuilesNermine'


// Tableau de bord de l'admin : tableau des chiffres + derniers inscrits
export default function Dashboard() {
  const [utilisateurs, setUtilisateurs] = useState(null)
  const [erreur, setErreur] = useState('')

  useEffect(() => {
    getUtilisateurs().then(setUtilisateurs).catch((err) => setErreur(err.message))
  }, [])

  const adherents = (utilisateurs || []).filter((u) => u.role === 'ADHERENT')
  const coachs = (utilisateurs || []).filter((u) => u.role === 'COACH')
  // Les id sont croissants : les plus grands sont les derniers comptes crees
  const derniers = [...adherents].sort((a, b) => b.id - a.id).slice(0, 5)

  const aujourdhui = new Date().toLocaleDateString('fr-FR', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' })

  return (
    <>
      <div className="page-head">
        <div>
          <p className="surtitre" style={{ margin: '0 0 8px' }}>{aujourdhui}</p>
          <h1>Tableau de bord</h1>
        </div>
      </div>

      {erreur && <p className="error">{erreur}</p>}
      {!utilisateurs && !erreur && <Loader />}

      {utilisateurs && (
        <>
          <div className="tiles stagger">
            <Link to="/adherents" className="tile tile-hot" style={{ '--i': 0 }}>
              <span className="tile-label">Adhérents</span>
              <span className="tile-value">{adherents.length}</span>
              <span className="tile-sub">comptes actifs</span>
            </Link>
            <Link to="/coachs" className="tile" style={{ '--i': 1 }}>
              <span className="tile-label">Coachs</span>
              <span className="tile-value">{coachs.length}</span>
              <span className="tile-sub">dans l'équipe</span>
            </Link>
            {/* [eya] tuiles */}
            <TuileCours />

            {/* [nermine] tuiles */}
            <TuilesNermine />

          </div>

          <h2 className="section-sous-titre">Derniers inscrits</h2>
          <div className="card stagger">
            {derniers.length === 0 && <p className="muted" style={{ padding: 16 }}>Aucun adhérent pour le moment.</p>}
            {derniers.map((a, i) => (
              <div key={a.id} className="ligne-personne" style={{ '--i': i }}>
                <Avatar personne={a} taille="sm" />
                <div>
                  <div className="personne-nom">{a.prenom} {a.nom}</div>
                  <div className="personne-email">{a.email}</div>
                </div>
              </div>
            ))}
            <Link to="/adherents" className="ligne-lien">Voir tous les adhérents</Link>
          </div>
        </>
      )}
    </>
  )
}
