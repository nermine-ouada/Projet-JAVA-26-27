import { useEffect, useState } from 'react'
import { getSalles, createSalle, updateSalle, deleteSalle } from '../api'
import { useAuth } from '../AuthContext'
import Modal from '../components/Modal'

const VIDE = { nom: '', capacite: '' }

// CRUD des salles : tout le monde consulte, seul l'admin modifie
export default function Salles() {
  const { estAdmin } = useAuth()
  const [salles, setSalles] = useState([])
  const [erreur, setErreur] = useState('')
  const [chargement, setChargement] = useState(true)

  const [edition, setEdition] = useState(null)      // null = fermé, sinon le formulaire
  const [aSupprimer, setASupprimer] = useState(null) // salle à confirmer
  const [erreurForm, setErreurForm] = useState('')

  async function charger() {
    try {
      setSalles(await getSalles())
      setErreur('')
    } catch (err) {
      setErreur(err.message)
    } finally {
      setChargement(false)
    }
  }

  useEffect(() => { charger() }, [])

  function ouvrir(salle) {
    setErreurForm('')
    setEdition(salle ? { ...salle } : VIDE)
  }

  async function enregistrer(e) {
    e.preventDefault()
    const salle = { nom: edition.nom, capacite: Number(edition.capacite) }
    try {
      if (edition.id) await updateSalle(edition.id, salle)
      else await createSalle(salle)
      setEdition(null)
      charger()
    } catch (err) {
      setErreurForm(err.message)
    }
  }

  async function supprimer() {
    try {
      await deleteSalle(aSupprimer.id)
      setASupprimer(null)
      charger()
    } catch (err) {
      setErreurForm(err.message)
    }
  }

  return (
    <>
      <div className="page-head">
        <div>
          <h1>Salles</h1>
          <p className="muted">{salles.length} salle(s) dans le club</p>
        </div>
        {estAdmin && (
          <button className="btn btn-primary" onClick={() => ouvrir(null)}>Nouvelle salle</button>
        )}
      </div>

      {erreur && <p className="error">{erreur}</p>}

      <div className="card">
        <table className="table">
          <thead>
            <tr>
              <th>Nom</th>
              <th className="num">Capacité</th>
              {estAdmin && <th className="actions"></th>}
            </tr>
          </thead>
          <tbody>
            {chargement && <tr><td colSpan="3" className="empty">Chargement…</td></tr>}
            {!chargement && salles.length === 0 && (
              <tr><td colSpan="3" className="empty">Aucune salle pour le moment.</td></tr>
            )}
            {salles.map((s) => (
              <tr key={s.id}>
                <td className="strong">{s.nom}</td>
                <td className="num">{s.capacite} places</td>
                {estAdmin && (
                  <td className="actions">
                    <button className="btn btn-small" onClick={() => ouvrir(s)}>Modifier</button>
                    <button className="btn btn-small btn-danger-ghost"
                      onClick={() => { setErreurForm(''); setASupprimer(s) }}>
                      Supprimer
                    </button>
                  </td>
                )}
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {edition && (
        <Modal titre={edition.id ? 'Modifier la salle' : 'Nouvelle salle'} onClose={() => setEdition(null)}>
          <form className="form" onSubmit={enregistrer}>
            <label>
              Nom
              <input value={edition.nom} required autoFocus
                onChange={(e) => setEdition({ ...edition, nom: e.target.value })} />
            </label>
            <label>
              Capacité (places)
              <input type="number" min="1" value={edition.capacite} required
                onChange={(e) => setEdition({ ...edition, capacite: e.target.value })} />
            </label>
            {erreurForm && <p className="error">{erreurForm}</p>}
            <div className="modal-actions">
              <button type="button" className="btn" onClick={() => setEdition(null)}>Annuler</button>
              <button className="btn btn-primary">Enregistrer</button>
            </div>
          </form>
        </Modal>
      )}

      {aSupprimer && (
        <Modal titre="Supprimer la salle" onClose={() => setASupprimer(null)}>
          <p>Supprimer <strong>{aSupprimer.nom}</strong> ? Cette action est définitive.</p>
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
