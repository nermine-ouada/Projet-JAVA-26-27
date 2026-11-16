import { useEffect, useState } from 'react'
import { getAbonnements, createAbonnement, updateAbonnement, deleteAbonnement, getAdherents } from '../api'
import Modal from '../components/Modal'

// Formules proposees : prix et duree par defaut (modifiables dans le formulaire)
export const FORMULES = {
  Mensuel: { prix: 80, mois: 1 },
  Trimestriel: { prix: 210, mois: 3 },
  Annuel: { prix: 700, mois: 12 },
}

export const STATUTS = {
  ACTIF: 'Actif',
  EXPIRE: 'Expiré',
  A_VENIR: 'À venir',
}

// "2026-11-09" -> "9 nov. 2026"
export function formaterDate(iso) {
  return new Date(iso + 'T00:00').toLocaleDateString('fr-FR', { day: 'numeric', month: 'short', year: 'numeric' })
}

// Date locale -> "AAAA-MM-JJ" (sans passer par l'UTC, qui decalerait d'un jour)
function versIso(d) {
  const mois = String(d.getMonth() + 1).padStart(2, '0')
  const jour = String(d.getDate()).padStart(2, '0')
  return d.getFullYear() + '-' + mois + '-' + jour
}

// Ajoute des mois a une date "AAAA-MM-JJ"
function ajouterMois(iso, mois) {
  const d = new Date(iso + 'T00:00')
  d.setMonth(d.getMonth() + mois)
  return versIso(d)
}

function aujourdhui() {
  return versIso(new Date())
}

function formulaireVide() {
  const debut = aujourdhui()
  return { type: 'Mensuel', dateDebut: debut, dateFin: ajouterMois(debut, 1), prix: 80, adherentId: '' }
}

// CRUD des abonnements (page reservee a l'admin)
export default function Abonnements() {
  const [abonnements, setAbonnements] = useState([])
  const [adherents, setAdherents] = useState([])
  const [erreur, setErreur] = useState('')
  const [chargement, setChargement] = useState(true)

  const [edition, setEdition] = useState(null)
  const [aSupprimer, setASupprimer] = useState(null)
  const [erreurForm, setErreurForm] = useState('')

  async function charger() {
    try {
      setAbonnements(await getAbonnements())
      setErreur('')
    } catch (err) {
      setErreur(err.message)
    } finally {
      setChargement(false)
    }
  }

  useEffect(() => {
    charger()
    getAdherents().then(setAdherents).catch(() => {})
  }, [])

  function ouvrir(ab) {
    setErreurForm('')
    setEdition(ab ? { ...ab } : formulaireVide())
  }

  function changer(e) {
    setEdition({ ...edition, [e.target.name]: e.target.value })
  }

  // Changer de formule met a jour le prix et la date de fin
  function changerType(e) {
    const type = e.target.value
    const formule = FORMULES[type]
    setEdition({ ...edition, type, prix: formule.prix, dateFin: ajouterMois(edition.dateDebut, formule.mois) })
  }

  async function enregistrer(e) {
    e.preventDefault()
    const corps = {
      type: edition.type,
      dateDebut: edition.dateDebut,
      dateFin: edition.dateFin,
      prix: Number(edition.prix),
      adherentId: Number(edition.adherentId),
    }
    try {
      if (edition.id) await updateAbonnement(edition.id, corps)
      else await createAbonnement(corps)
      setEdition(null)
      charger()
    } catch (err) {
      setErreurForm(err.message)
    }
  }

  async function supprimer() {
    try {
      await deleteAbonnement(aSupprimer.id)
      setASupprimer(null)
      charger()
    } catch (err) {
      setErreurForm(err.message)
    }
  }

  const actifs = abonnements.filter((a) => a.statut === 'ACTIF').length

  return (
    <>
      <div className="page-head">
        <div>
          <h1>Abonnements</h1>
          <p className="muted">{abonnements.length} abonnement(s), dont {actifs} actif(s)</p>
        </div>
        <button className="btn btn-primary" onClick={() => ouvrir(null)}>Nouvel abonnement</button>
      </div>

      {erreur && <p className="error">{erreur}</p>}

      <div className="card">
        <table className="table">
          <thead>
            <tr>
              <th>Adhérent</th>
              <th>Formule</th>
              <th>Période</th>
              <th className="num">Prix</th>
              <th>Statut</th>
              <th className="actions"></th>
            </tr>
          </thead>
          <tbody>
            {chargement && <tr><td colSpan="6" className="empty">Chargement…</td></tr>}
            {!chargement && abonnements.length === 0 && (
              <tr><td colSpan="6" className="empty">Aucun abonnement.</td></tr>
            )}
            {abonnements.map((a) => (
              <tr key={a.id}>
                <td className="strong">{a.adherentNom}</td>
                <td>{a.type}</td>
                <td>{formaterDate(a.dateDebut)} au {formaterDate(a.dateFin)}</td>
                <td className="num">{Number(a.prix).toFixed(2)} DT</td>
                <td><span className={'statut statut-' + a.statut.toLowerCase()}>{STATUTS[a.statut]}</span></td>
                <td className="actions">
                  <button className="btn btn-small" onClick={() => ouvrir(a)}>Modifier</button>
                  <button className="btn btn-small btn-danger-ghost" aria-label="Supprimer"
                    onClick={() => { setErreurForm(''); setASupprimer(a) }}>Supprimer</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {edition && (
        <Modal titre={edition.id ? "Modifier l'abonnement" : 'Nouvel abonnement'} onClose={() => setEdition(null)}>
          <form className="form" onSubmit={enregistrer}>
            <label>
              Adhérent
              <select name="adherentId" value={edition.adherentId} onChange={changer} required autoFocus>
                <option value="">Choisir…</option>
                {adherents.map((a) => (
                  <option key={a.id} value={a.id}>{a.prenom} {a.nom} ({a.email})</option>
                ))}
              </select>
            </label>
            <div className="row">
              <label>
                Formule
                <select name="type" value={edition.type} onChange={changerType}>
                  {Object.keys(FORMULES).map((t) => <option key={t} value={t}>{t}</option>)}
                </select>
              </label>
              <label>
                Prix (DT)
                <input name="prix" type="number" min="1" step="0.01" value={edition.prix} onChange={changer} required />
              </label>
            </div>
            <div className="row">
              <label>
                Début
                <input name="dateDebut" type="date" value={edition.dateDebut} onChange={changer} required />
              </label>
              <label>
                Fin
                <input name="dateFin" type="date" value={edition.dateFin} onChange={changer} required />
              </label>
            </div>
            {erreurForm && <p className="error">{erreurForm}</p>}
            <div className="modal-actions">
              <button type="button" className="btn" onClick={() => setEdition(null)}>Annuler</button>
              <button className="btn btn-primary">Enregistrer</button>
            </div>
          </form>
        </Modal>
      )}

      {aSupprimer && (
        <Modal titre="Supprimer l'abonnement" onClose={() => setASupprimer(null)}>
          <p>Supprimer l'abonnement <strong>{aSupprimer.type}</strong> de <strong>{aSupprimer.adherentNom}</strong> ?
            Cette action est définitive.</p>
          {erreurForm && <p className="error">{erreurForm}</p>}
          <div className="modal-actions">
            <button className="btn" onClick={() => setASupprimer(null)}>Annuler</button>
            <button className="btn btn-danger" onClick={supprimer}>Supprimer</button>
          </div>
        </Modal>
      )}
    </>
  )
}
