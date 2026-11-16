import { useEffect, useState } from 'react'
import { getSalles, createSalle, updateSalle, deleteSalle, getAllCours } from '../api'
import { useAuth } from '../AuthContext'
import Modal from '../components/Modal'
import Loader from '../components/Loader'

const VIDE = { nom: '', capacite: '' }
// Emplacements du plan (voir .plan dans styles.css) : les salles y sont placees dans l'ordre
const EMPLACEMENTS = ['s1', 's2', 's3', 's4', 's5']

// Prochain cours d'une salle (a partir de maintenant)
function prochainCours(cours, salleId) {
  const maintenant = new Date()
  return cours
    .filter((c) => c.salleId === salleId && new Date(c.dateCours + 'T' + c.heure) >= maintenant)
    .sort((a, b) => (a.dateCours + a.heure).localeCompare(b.dateCours + b.heure))[0]
}

function jourCourt(iso) {
  return new Date(iso + 'T00:00').toLocaleDateString('fr-FR', { weekday: 'short', day: 'numeric' })
}

// Salles : plan de la salle de sport (pour la presentation) + CRUD (admin)
export default function Salles() {
  const { estAdmin } = useAuth()
  const [salles, setSalles] = useState([])
  const [cours, setCours] = useState([])
  const [erreur, setErreur] = useState('')
  const [chargement, setChargement] = useState(true)
  const [survol, setSurvol] = useState(null) // salle survolee : mise en evidence sur le plan et dans le tableau

  const [edition, setEdition] = useState(null)
  const [aSupprimer, setASupprimer] = useState(null)
  const [erreurForm, setErreurForm] = useState('')

  async function charger() {
    try {
      // Ordre de creation : la premiere salle prend le grand emplacement du plan
      setSalles((await getSalles()).sort((a, b) => a.id - b.id))
      setErreur('')
    } catch (err) {
      setErreur(err.message)
    } finally {
      setChargement(false)
    }
  }

  useEffect(() => {
    charger()
    getAllCours().then(setCours).catch(() => {})
  }, [])

  function ouvrir(salle) {
    if (!estAdmin) return
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

  const placeTotale = salles.reduce((n, s) => n + s.capacite, 0)
  const surPlan = salles.slice(0, EMPLACEMENTS.length)

  return (
    <>
      <div className="page-head">
        <div>
          <h1>Salles</h1>
          <p className="muted">{salles.length} salle(s), {placeTotale} places au total</p>
        </div>
        {estAdmin && (
          <button className="btn btn-primary" onClick={() => ouvrir(null)}>Nouvelle salle</button>
        )}
      </div>

      {erreur && <p className="error">{erreur}</p>}
      {chargement && <Loader />}

      {!chargement && (
        <>
          {/* ---------- Plan de la salle de sport (illustration) ---------- */}
          <section className="plan-carte">
            <div className="plan-entete">
              <h2>Plan du club</h2>
              <span className="hint">Plan indicatif, rez-de-chaussée</span>
            </div>
            <div className="plan">
              {surPlan.map((s, i) => {
                const prochain = prochainCours(cours, s.id)
                return (
                  <button key={s.id} type="button"
                    className={'plan-salle ' + EMPLACEMENTS[i] + (survol === s.id ? ' survol' : '') + (estAdmin ? '' : ' lecture')}
                    onMouseEnter={() => setSurvol(s.id)} onMouseLeave={() => setSurvol(null)}
                    onClick={() => ouvrir(s)}>
                    <span className="plan-nom">{s.nom}</span>
                    <span className="plan-capacite">{s.capacite} places</span>
                    <span className="plan-prochain">
                      {prochain ? 'Prochain : ' + prochain.nomCours + ', ' + jourCourt(prochain.dateCours) + ' ' + prochain.heure.slice(0, 5) : 'Aucun cours prévu'}
                    </span>
                  </button>
                )
              })}
              {/* Espaces fixes du club, dessines pour l'exemple */}
              <div className="plan-zone vestiaires">Vestiaires</div>
              <div className="plan-zone accueil">Accueil<span className="plan-porte" aria-hidden="true" /></div>
            </div>
            {salles.length > EMPLACEMENTS.length && (
              <p className="hint" style={{ marginTop: 10 }}>
                Le plan montre les {EMPLACEMENTS.length} premières salles ; toutes sont listées ci-dessous.
              </p>
            )}
          </section>

          {/* ---------- Liste des salles ---------- */}
          <div className="card">
            <table className="table">
              <thead>
                <tr>
                  <th>Nom</th>
                  <th className="num">Capacité</th>
                  <th className="hide-sm">Prochain cours</th>
                  {estAdmin && <th className="actions"></th>}
                </tr>
              </thead>
              <tbody>
                {salles.length === 0 && (
                  <tr><td colSpan="4" className="empty">Aucune salle pour le moment.</td></tr>
                )}
                {salles.map((s) => {
                  const prochain = prochainCours(cours, s.id)
                  return (
                    <tr key={s.id} className={survol === s.id ? 'ligne-survol' : ''}
                      onMouseEnter={() => setSurvol(s.id)} onMouseLeave={() => setSurvol(null)}>
                      <td className="strong">{s.nom}</td>
                      <td className="num">{s.capacite} places</td>
                      <td className="hide-sm muted">
                        {prochain ? prochain.nomCours + ', ' + jourCourt(prochain.dateCours) + ' à ' + prochain.heure.slice(0, 5) : 'Aucun'}
                      </td>
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
                  )
                })}
              </tbody>
            </table>
          </div>
        </>
      )}

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
