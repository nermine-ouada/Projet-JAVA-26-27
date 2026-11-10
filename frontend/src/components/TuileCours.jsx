import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { CalendarDays } from 'lucide-react'
import { getAllCours } from '../api'

// Tuile du tableau de bord : cours des 7 prochains jours et places reservees
export default function TuileCours() {
  const [cours, setCours] = useState(null)

  useEffect(() => {
    getAllCours().then(setCours).catch(() => setCours([]))
  }, [])

  const maintenant = new Date()
  const dans7jours = new Date(maintenant.getTime() + 7 * 24 * 3600 * 1000)
  const prochains = (cours || []).filter((c) => {
    const d = new Date(c.dateCours + 'T' + c.heure)
    return d >= maintenant && d <= dans7jours
  })
  const places = prochains.reduce((n, c) => n + c.inscrits, 0)

  return (
    <Link to="/planning" className="tile" style={{ '--i': 2 }}>
      <span className="tile-label"><CalendarDays size={16} aria-hidden="true" /> Cours à venir</span>
      <span className="tile-value">{cours ? prochains.length : '…'}</span>
      <span className="tile-sub">sur 7 jours, {places} place(s) réservée(s)</span>
    </Link>
  )
}
