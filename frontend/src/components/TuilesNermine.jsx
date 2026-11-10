import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { Ticket, Handshake } from 'lucide-react'
import { getAbonnements, getSeances } from '../api'

// Tuiles du tableau de bord : abonnements actifs et seances privees a confirmer
export default function TuilesNermine() {
  const [abonnements, setAbonnements] = useState(null)
  const [seances, setSeances] = useState(null)

  useEffect(() => {
    getAbonnements().then(setAbonnements).catch(() => setAbonnements([]))
    getSeances().then(setSeances).catch(() => setSeances([]))
  }, [])

  const actifs = (abonnements || []).filter((a) => a.statut === 'ACTIF').length
  const maintenant = new Date()
  const aVenir = (seances || []).filter((s) => new Date(s.dateSeance + 'T' + s.heure) >= maintenant)
  const enAttente = aVenir.filter((s) => s.statut === 'EN_ATTENTE').length

  return (
    <>
      <Link to="/abonnements" className="tile" style={{ '--i': 3 }}>
        <span className="tile-label"><Ticket size={16} aria-hidden="true" /> Abonnements actifs</span>
        <span className="tile-value">{abonnements ? actifs : '…'}</span>
        <span className="tile-sub">sur {abonnements ? abonnements.length : '…'} abonnement(s)</span>
      </Link>
      <Link to="/seances" className={'tile' + (enAttente > 0 ? ' tile-hot' : '')} style={{ '--i': 4 }}>
        <span className="tile-label"><Handshake size={16} aria-hidden="true" /> Séances à confirmer</span>
        <span className="tile-value">{seances ? enAttente : '…'}</span>
        <span className="tile-sub">{seances ? aVenir.length : '…'} séance(s) privée(s) à venir</span>
      </Link>
    </>
  )
}
