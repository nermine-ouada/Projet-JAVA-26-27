import { useEffect, useState } from 'react'
import { getAbonnements } from '../api'
import { STATUTS, formaterDate } from '../pages/Abonnements'

// Abonnements de l'adherent connecte (affiche sur "Mon profil")
export default function MesAbonnements() {
  const [abonnements, setAbonnements] = useState([])
  const [erreur, setErreur] = useState('')

  useEffect(() => {
    getAbonnements().then(setAbonnements).catch((err) => setErreur(err.message))
  }, [])

  return (
    <section className="mes-abonnements">
      <h2 className="section-sous-titre">Mes abonnements</h2>
      {erreur && <p className="error">{erreur}</p>}
      {!erreur && abonnements.length === 0 && (
        <div className="card empty">Aucun abonnement pour le moment. Passez à l'accueil du club pour en souscrire un.</div>
      )}
      <div className="abonnement-cartes">
        {abonnements.map((a) => (
          <div key={a.id} className={'card abonnement-carte' + (a.statut === 'ACTIF' ? ' abonnement-actif' : '')}>
            <div className="abonnement-haut">
              <strong>{a.type}</strong>
              <span className={'statut statut-' + a.statut.toLowerCase()}>{STATUTS[a.statut]}</span>
            </div>
            <p className="muted">{formaterDate(a.dateDebut)} au {formaterDate(a.dateFin)}</p>
            <p className="abonnement-prix">{Number(a.prix).toFixed(2)} DT</p>
          </div>
        ))}
      </div>
    </section>
  )
}
