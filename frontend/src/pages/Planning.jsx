import { useEffect, useState } from 'react'
import { Plus, Pencil, Trash2, Clock, MapPin, User, Users } from 'lucide-react'
import { getAllCours, createCours, updateCours, deleteCours, getSalles, getCoachs } from '../api'
import { useAuth } from '../AuthContext'
import Modal from '../components/Modal'

const VIDE = { nomCours: '', dateCours: '', heure: '', capacite: '', coachId: '', salleId: '' }

// "2026-11-16" -> "lundi 16 novembre"
function formaterDate(iso) {
  return new Date(iso + 'T00:00').toLocaleDateString('fr-FR', { weekday: 'long', day: 'numeric', month: 'long' })
}

// Regroupe les cours par date : { "2026-11-16": [cours, cours], ... }
function grouperParDate(cours) {
  const groupes = {}
  for (const c of cours) {
    if (!groupes[c.dateCours]) groupes[c.dateCours] = []
    groupes[c.dateCours].push(c)
  }
  return groupes
}

// Planning des cours : tout le monde consulte, seul l'admin modifie
export default function Planning() {
  const { estAdmin } = useAuth()
  const [cours, setCours] = useState([])
  const [salles, setSalles] = useState([])
  const [coachs, setCoachs] = useState([])
  const [erreur, setErreur] = useState('')
  const [chargement, setChargement] = useState(true)

  const [edition, setEdition] = useState(null)
  const [aSupprimer, setASupprimer] = useState(null)
  const [erreurForm, setErreurForm] = useState('')

  async function charger() {
    try {
      setCours(await getAllCours())
      setErreur('')
    } catch (err) {
      setErreur(err.message)
    } finally {
      setChargement(false)
    }
  }

  useEffect(() => {
    charger()
    // Listes pour les menus deroulants du formulaire (admin seulement)
    if (estAdmin) {
      getSalles().then(setSalles).catch(() => {})
      getCoachs().then(setCoachs).catch(() => {})
    }
  }, [estAdmin])

  function ouvrir(c) {
    setErreurForm('')
    setEdition(c ? { ...c, heure: c.heure.slice(0, 5) } : VIDE)
  }

  function changer(e) {
    setEdition({ ...edition, [e.target.name]: e.target.value })
  }

  async function enregistrer(e) {
    e.preventDefault()
    const corps = {
      nomCours: edition.nomCours,
      dateCours: edition.dateCours,
      heure: edition.heure,
      capacite: Number(edition.capacite),
      coachId: Number(edition.coachId),
      salleId: Number(edition.salleId),
    }
    try {
      if (edition.id) await updateCours(edition.id, corps)
      else await createCours(corps)
      setEdition(null)
      charger()
    } catch (err) {
      setErreurForm(err.message)
    }
  }

  async function supprimer() {
    try {
      await deleteCours(aSupprimer.id)
      setASupprimer(null)
      charger()
    } catch (err) {
      setErreurForm(err.message)
    }
  }

  const groupes = grouperParDate(cours)

  return (
    <>
      <div className="page-head">
        <div>
          <h1>Planning</h1>
          <p className="muted">{cours.length} cours programmé(s)</p>
        </div>
        {estAdmin && (
          <button className="btn btn-primary" onClick={() => ouvrir(null)}><Plus size={16} /> Nouveau cours</button>
        )}
      </div>

      {erreur && <p className="error">{erreur}</p>}
      {chargement && <div className="card empty">Chargement…</div>}
      {!chargement && cours.length === 0 && <div className="card empty">Aucun cours programmé.</div>}

      {Object.entries(groupes).map(([date, liste]) => (
        <section key={date} className="jour">
          <h2 className="jour-titre">{formaterDate(date)}</h2>
          <div className="card">
            {liste.map((c) => (
              <div key={c.id} className="cours-ligne">
                <span className="cours-heure"><Clock size={15} /> {c.heure.slice(0, 5)}</span>
                <div className="cours-info">
                  <strong>{c.nomCours}</strong>
                  <span className="muted cours-details">
                    <span><User size={13} /> {c.coachNom}</span>
                    <span><MapPin size={13} /> {c.salleNom}</span>
                    <span><Users size={13} /> {c.capacite} places</span>
                  </span>
                </div>
                {estAdmin && (
                  <div className="actions">
                    <button className="btn btn-small" onClick={() => ouvrir(c)}><Pencil size={14} /></button>
                    <button className="btn btn-small btn-danger-ghost" aria-label="Supprimer"
                      onClick={() => { setErreurForm(''); setASupprimer(c) }}>
                      <Trash2 size={14} />
                    </button>
                  </div>
                )}
              </div>
            ))}
          </div>
        </section>
      ))}

      {edition && (
        <Modal titre={edition.id ? 'Modifier le cours' : 'Nouveau cours'} onClose={() => setEdition(null)}>
          <form className="form" onSubmit={enregistrer}>
            <label>
              Nom du cours
              <input name="nomCours" value={edition.nomCours} onChange={changer} required autoFocus />
            </label>
            <div className="row">
              <label>
                Date
                <input name="dateCours" type="date" value={edition.dateCours} onChange={changer} required />
              </label>
              <label>
                Heure
                <input name="heure" type="time" value={edition.heure} onChange={changer} required />
              </label>
            </div>
            <div className="row">
              <label>
                Coach
                <select name="coachId" value={edition.coachId} onChange={changer} required>
                  <option value="">Choisir…</option>
                  {coachs.map((c) => (
                    <option key={c.id} value={c.id}>{c.prenom} {c.nom} ({c.specialite})</option>
                  ))}
                </select>
              </label>
              <label>
                Salle
                <select name="salleId" value={edition.salleId} onChange={changer} required>
                  <option value="">Choisir…</option>
                  {salles.map((s) => (
                    <option key={s.id} value={s.id}>{s.nom} ({s.capacite} pl.)</option>
                  ))}
                </select>
              </label>
            </div>
            <label>
              Capacité <span className="hint">ne doit pas dépasser celle de la salle</span>
              <input name="capacite" type="number" min="1" value={edition.capacite} onChange={changer} required />
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
        <Modal titre="Supprimer le cours" onClose={() => setASupprimer(null)}>
          <p>Supprimer <strong>{aSupprimer.nomCours}</strong> du {formaterDate(aSupprimer.dateCours)} ?
            Les inscriptions à ce cours seront aussi supprimées.</p>
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
