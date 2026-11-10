import { useEffect, useState } from 'react'
import { CalendarDays } from 'lucide-react'
import {
  getAllCours, createCours, updateCours, deleteCours, getSalles, getCoachs,
  inscrireCours, desinscrireCours, getParticipants,
} from '../api'
import { useAuth } from '../AuthContext'
import Modal from '../components/Modal'
import Avatar from '../components/Avatar'
import Loader from '../components/Loader'
import EmptyState from '../components/EmptyState'

const VIDE = { nomCours: '', dateCours: '', heure: '', capacite: '', coachId: '', salleId: '', semaines: 1 }
const NOMS_JOURS = ['Lun', 'Mar', 'Mer', 'Jeu', 'Ven', 'Sam', 'Dim']

// ---------- Outils de dates (toujours en heure locale) ----------

// Date -> "AAAA-MM-JJ" sans passer par l'UTC (qui decalerait d'un jour)
function versIso(d) {
  return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0')
}

// Lundi de la semaine d'une date
function lundiDe(date) {
  const d = new Date(date)
  d.setHours(0, 0, 0, 0)
  d.setDate(d.getDate() - ((d.getDay() + 6) % 7))
  return d
}

function ajouterJours(date, n) {
  const d = new Date(date)
  d.setDate(d.getDate() + n)
  return d
}

// "2026-11-16" -> "Lundi 16 novembre" (majuscule seulement au debut)
function dateLongue(iso) {
  const texte = new Date(iso + 'T00:00').toLocaleDateString('fr-FR', { weekday: 'long', day: 'numeric', month: 'long' })
  return texte.charAt(0).toUpperCase() + texte.slice(1)
}

// "18:30" -> 1110 (minutes depuis minuit)
function minutes(heure) {
  const [h, m] = heure.split(':').map(Number)
  return h * 60 + m
}

// Un cours occupe sa salle pendant 1 heure (meme regle que le backend)
const DUREE_COURS = 60

// Cours qui occupe deja cette salle a cette date et heure (sauf le cours en cours de modification)
function conflitSalle(cours, salleId, date, heure, coursExcluId) {
  if (!salleId || !date || !heure) return null
  return cours.find((c) => c.salleId === Number(salleId) && c.dateCours === date && c.id !== coursExcluId
    && Math.abs(minutes(c.heure) - minutes(heure)) < DUREE_COURS) || null
}

function finCours(heure) {
  const fin = minutes(heure) + DUREE_COURS
  return String(Math.floor(fin / 60) % 24).padStart(2, '0') + ':' + String(fin % 60).padStart(2, '0')
}

function estPasse(c) {
  return new Date(c.dateCours + 'T' + c.heure) < new Date()
}

// Message sous le choix de la salle : libre ou occupee (et par quel cours)
function DisponibiliteSalle({ cours, edition }) {
  if (!edition.salleId || !edition.dateCours || !edition.heure) {
    return <span className="dispo">Choisissez la date et l'heure pour voir les salles libres.</span>
  }
  const conflit = conflitSalle(cours, edition.salleId, edition.dateCours, edition.heure, edition.id)
  if (conflit) {
    return (
      <span className="dispo dispo-occupee">
        Occupée de {conflit.heure.slice(0, 5)} à {finCours(conflit.heure)} ({conflit.nomCours})
      </span>
    )
  }
  return <span className="dispo dispo-libre">Salle libre de {edition.heure} à {finCours(edition.heure)}</span>
}

// Agenda de la semaine : tout le monde consulte, l'adherent s'inscrit, l'admin planifie
export default function Planning() {
  const { utilisateur, estAdmin, estCoach, estAdherent } = useAuth()
  const [lundi, setLundi] = useState(() => lundiDe(new Date()))
  const [cours, setCours] = useState([])
  const [salles, setSalles] = useState([])
  const [coachs, setCoachs] = useState([])
  const [filtre, setFiltre] = useState(estCoach ? 'miens' : 'tous')
  const [erreur, setErreur] = useState('')
  const [chargement, setChargement] = useState(true)

  const [selection, setSelection] = useState(null)     // cours ouvert dans la fiche
  const [participants, setParticipants] = useState(null)
  const [message, setMessage] = useState('')
  const [edition, setEdition] = useState(null)         // formulaire de creation / modification
  const [aSupprimer, setASupprimer] = useState(null)
  const [erreurForm, setErreurForm] = useState('')
  const [enCours, setEnCours] = useState(false)

  async function charger() {
    try {
      const liste = await getAllCours()
      setCours(liste)
      setErreur('')
      return liste
    } catch (err) {
      setErreur(err.message)
      return []
    } finally {
      setChargement(false)
    }
  }

  useEffect(() => {
    charger()
    if (estAdmin) {
      getSalles().then(setSalles).catch(() => {})
      getCoachs().then(setCoachs).catch(() => {})
    }
  }, [estAdmin])

  // ---------- Fiche d'un cours ----------

  function ouvrirFiche(c) {
    setSelection(c)
    setMessage('')
    setErreurForm('')
    setParticipants(null)
    if (!estAdherent) {
      getParticipants(c.id).then(setParticipants).catch(() => setParticipants([]))
    }
  }

  async function reserver(inscrire) {
    setEnCours(true)
    setErreurForm('')
    try {
      if (inscrire) await inscrireCours(selection.id)
      else await desinscrireCours(selection.id)
      const liste = await charger()
      setSelection(liste.find((c) => c.id === selection.id) || null)
      setMessage(inscrire ? 'Votre place est réservée.' : 'Votre inscription est annulée.')
    } catch (err) {
      setErreurForm(err.message)
    } finally {
      setEnCours(false)
    }
  }

  // ---------- Formulaire (admin) ----------

  function ouvrirFormulaire(c, date) {
    setErreurForm('')
    setSelection(null)
    if (c) setEdition({ ...c, heure: c.heure.slice(0, 5), semaines: 1 })
    else setEdition({ ...VIDE, dateCours: date || '' })
  }

  function changer(e) {
    setEdition({ ...edition, [e.target.name]: e.target.value })
  }

  async function enregistrer(e) {
    e.preventDefault()
    setEnCours(true)
    const corps = {
      nomCours: edition.nomCours,
      dateCours: edition.dateCours,
      heure: edition.heure,
      capacite: Number(edition.capacite),
      coachId: Number(edition.coachId),
      salleId: Number(edition.salleId),
      semaines: Number(edition.semaines),
    }
    try {
      if (edition.id) await updateCours(edition.id, corps)
      else await createCours(corps)
      setEdition(null)
      // Affiche la semaine du cours enregistre
      setLundi(lundiDe(new Date(corps.dateCours + 'T00:00')))
      charger()
    } catch (err) {
      setErreurForm(err.message)
    } finally {
      setEnCours(false)
    }
  }

  async function supprimer() {
    setEnCours(true)
    try {
      await deleteCours(aSupprimer.id)
      setASupprimer(null)
      charger()
    } catch (err) {
      setErreurForm(err.message)
    } finally {
      setEnCours(false)
    }
  }

  // ---------- Donnees de la semaine affichee ----------

  const jours = NOMS_JOURS.map((nom, i) => {
    const date = ajouterJours(lundi, i)
    return { nom, date, iso: versIso(date) }
  })
  const aujourdhui = versIso(new Date())
  const dimanche = jours[6].date

  const visibles = cours.filter((c) => {
    if (filtre === 'miens' && estAdherent) return c.inscrit
    if (filtre === 'miens' && estCoach) return c.coachUtilisateurId === utilisateur.id
    return true
  })
  const coursDuJour = (iso) => visibles.filter((c) => c.dateCours === iso).sort((a, b) => a.heure.localeCompare(b.heure))
  const totalSemaine = jours.reduce((n, j) => n + coursDuJour(j.iso).length, 0)

  const titreSemaine = 'Semaine du ' + lundi.getDate() + ' au '
    + dimanche.toLocaleDateString('fr-FR', { day: 'numeric', month: 'long', year: 'numeric' })

  return (
    <>
      <div className="page-head">
        <div>
          <h1>{estAdherent ? 'Agenda des cours' : estCoach ? 'Mon planning' : 'Planning'}</h1>
          <p className="muted">{titreSemaine} · {totalSemaine} cours</p>
        </div>
        <div className="page-head-actions">
          {(estAdherent || estCoach) && (
            <div className="segmented" role="group" aria-label="Filtre">
              <button className={filtre === 'tous' ? 'actif' : ''} onClick={() => setFiltre('tous')}>Tous les cours</button>
              <button className={filtre === 'miens' ? 'actif' : ''} onClick={() => setFiltre('miens')}>
                {estAdherent ? 'Mes inscriptions' : 'Mes cours'}
              </button>
            </div>
          )}
          {estAdmin && (
            <button className="btn btn-primary" onClick={() => ouvrirFormulaire(null)}>
              Planifier un cours
            </button>
          )}
        </div>
      </div>

      <div className="semaine-nav">
        <button className="btn btn-small" onClick={() => setLundi(ajouterJours(lundi, -7))}>
          ‹ Précédente
        </button>
        <button className="btn btn-small" onClick={() => setLundi(lundiDe(new Date()))}>Cette semaine</button>
        <button className="btn btn-small" onClick={() => setLundi(ajouterJours(lundi, 7))}>
          Suivante ›
        </button>
      </div>

      {erreur && <p className="error">{erreur}</p>}
      {chargement && <Loader />}

      {!chargement && (
        <div className="agenda" key={versIso(lundi) + filtre}>
          {jours.map((j, i) => {
            const liste = coursDuJour(j.iso)
            const passe = j.iso < aujourdhui
            return (
              <section key={j.iso} className={'agenda-jour' + (j.iso === aujourdhui ? ' aujourdhui' : '') + (passe ? ' passe' : '')}
                style={{ '--i': i }}>
                <header className="agenda-jour-tete">
                  <span>{j.nom}</span>
                  <strong>{j.date.getDate()}</strong>
                </header>
                <div className="agenda-jour-cours">
                  {liste.map((c) => {
                    const complet = c.inscrits >= c.capacite
                    return (
                      <button key={c.id} className={'ccard' + (complet ? ' complet' : '') + (c.inscrit ? ' inscrit' : '') + (estPasse(c) ? ' fini' : '')}
                        onClick={() => ouvrirFiche(c)}>
                        <span className="ccard-heure">{c.heure.slice(0, 5)}</span>
                        <span className="ccard-nom">{c.nomCours}</span>
                        <span className="ccard-ligne">{c.coachNom} · {c.salleNom}</span>
                        <span className="ccard-places">
                          <span className="jauge"><span style={{ width: Math.min(100, (c.inscrits / c.capacite) * 100) + '%' }} /></span>
                          {complet ? 'Complet' : c.inscrits + '/' + c.capacite}
                        </span>
                        {c.inscrit && <span className="ccard-tag">Inscrit</span>}
                      </button>
                    )
                  })}
                  {liste.length === 0 && <span className="agenda-vide">Aucun cours</span>}
                  {estAdmin && !passe && (
                    <button className="agenda-ajout" onClick={() => ouvrirFormulaire(null, j.iso)} aria-label={'Ajouter un cours le ' + j.iso}>
                                          </button>
                  )}
                </div>
              </section>
            )
          })}
        </div>
      )}

      {!chargement && totalSemaine === 0 && filtre === 'miens' && (
        <div style={{ marginTop: 16 }}>
          <EmptyState icone={CalendarDays} titre={estAdherent ? 'Vous n\'êtes inscrit à aucun cours cette semaine.' : 'Aucun de vos cours cette semaine.'}>
            <button className="btn" onClick={() => setFiltre('tous')}>Voir tous les cours</button>
          </EmptyState>
        </div>
      )}

      {/* ---------- Fiche du cours ---------- */}
      {selection && (
        <Modal titre={selection.nomCours} onClose={() => setSelection(null)}>
          <div className="fiche">
            <p className="fiche-date">{dateLongue(selection.dateCours)} à {selection.heure.slice(0, 5)}</p>
            <div className="ucard-head">
              <Avatar personne={{ prenom: selection.coachNom, nom: selection.coachNom.split(' ')[1], photo: selection.coachPhoto }} />
              <div>
                <div className="ucard-title">{selection.coachNom}</div>
                <div className="ucard-sub">{selection.salleNom}</div>
              </div>
            </div>
            <div className="fiche-places">
              <span>{selection.inscrits} inscrit(s) sur {selection.capacite} places</span>
              <span className="jauge jauge-lg"><span style={{ width: Math.min(100, (selection.inscrits / selection.capacite) * 100) + '%' }} /></span>
            </div>

            {message && <p className="alert alert-success">{message}</p>}
            {erreurForm && <p className="error">{erreurForm}</p>}

            {/* Adherent : reserver ou annuler */}
            {estAdherent && (
              estPasse(selection) ? <p className="muted">Ce cours est terminé.</p>
                : selection.inscrit ? (
                  <button className="btn btn-block" onClick={() => reserver(false)} disabled={enCours}>Annuler mon inscription</button>
                ) : selection.inscrits >= selection.capacite ? (
                  <button className="btn btn-block" disabled>Cours complet</button>
                ) : (
                  <button className="btn btn-primary btn-block" onClick={() => reserver(true)} disabled={enCours}>
                    {enCours ? 'Réservation…' : 'Réserver ma place'}
                  </button>
                )
            )}

            {/* Admin et coach : liste des inscrits */}
            {!estAdherent && (
              <div>
                <h3 className="fiche-sous-titre">Inscrits</h3>
                {participants === null && <p className="muted">Chargement…</p>}
                {participants?.length === 0 && <p className="muted">Personne n'est encore inscrit.</p>}
                <div className="participants">
                  {participants?.map((p) => (
                    <span key={p.id} className="participant"><Avatar personne={p} taille="xs" /> {p.prenom} {p.nom}</span>
                  ))}
                </div>
              </div>
            )}

            {estAdmin && (
              <div className="modal-actions">
                <button className="btn btn-danger-ghost" onClick={() => { setErreurForm(''); setASupprimer(selection); setSelection(null) }}>
                  Supprimer
                </button>
                <button className="btn" onClick={() => ouvrirFormulaire(selection)}>
                  Modifier
                </button>
              </div>
            )}
          </div>
        </Modal>
      )}

      {/* ---------- Formulaire (admin) ---------- */}
      {edition && (
        <Modal titre={edition.id ? 'Modifier le cours' : 'Planifier un cours'} onClose={() => setEdition(null)}>
          <form className="form" onSubmit={enregistrer}>
            <label>
              Nom du cours
              <input name="nomCours" value={edition.nomCours} onChange={changer} required autoFocus placeholder="Yoga du soir" />
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
                    <option key={c.id} value={c.id}>{c.prenom} {c.nom}{c.specialite ? ' (' + c.specialite + ')' : ''}</option>
                  ))}
                </select>
              </label>
              <label>
                Salle
                <select name="salleId" value={edition.salleId} onChange={changer} required>
                  <option value="">Choisir…</option>
                  {salles.map((s) => {
                    const occupee = conflitSalle(cours, s.id, edition.dateCours, edition.heure, edition.id)
                    return (
                      <option key={s.id} value={s.id} disabled={!!occupee}>
                        {s.nom} ({s.capacite} pl.){occupee ? ' · occupée à ' + occupee.heure.slice(0, 5) : ''}
                      </option>
                    )
                  })}
                </select>
                <DisponibiliteSalle cours={cours} edition={edition} />
              </label>
            </div>
            <div className="row">
              <label>
                Capacité <span className="hint">au plus celle de la salle</span>
                <input name="capacite" type="number" min="1" value={edition.capacite} onChange={changer} required />
              </label>
              {!edition.id && (
                <label>
                  <span>Répéter</span>
                  <select name="semaines" value={edition.semaines} onChange={changer}>
                    <option value="1">Une seule fois</option>
                    {[2, 3, 4, 6, 8, 12].map((n) => <option key={n} value={n}>Chaque semaine, {n} semaines</option>)}
                  </select>
                </label>
              )}
            </div>
            {erreurForm && <p className="error">{erreurForm}</p>}
            <div className="modal-actions">
              <button type="button" className="btn" onClick={() => setEdition(null)}>Annuler</button>
              <button className="btn btn-primary" disabled={enCours}>{enCours ? 'Enregistrement…' : 'Enregistrer'}</button>
            </div>
          </form>
        </Modal>
      )}

      {aSupprimer && (
        <Modal titre="Supprimer le cours ?" onClose={() => setASupprimer(null)}>
          <p>Supprimer <strong>{aSupprimer.nomCours}</strong> du {dateLongue(aSupprimer.dateCours).toLowerCase()} ?
            {aSupprimer.inscrits > 0 && ' Les ' + aSupprimer.inscrits + ' inscription(s) seront aussi supprimées.'}</p>
          {erreurForm && <p className="error">{erreurForm}</p>}
          <div className="modal-actions">
            <button className="btn" onClick={() => setASupprimer(null)}>Annuler</button>
            <button className="btn btn-danger" onClick={supprimer} disabled={enCours}>Supprimer</button>
          </div>
        </Modal>
      )}
    </>
  )
}
