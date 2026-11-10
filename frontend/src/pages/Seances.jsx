import { useEffect, useState } from 'react'
import { Plus, Clock, Target, Check, X, Trash2, Handshake, Lock, CheckCircle2 } from 'lucide-react'
import { getSeances, createSeance, changerStatutSeance, deleteSeance, getCoachs, getAdherents } from '../api'
import { useAuth } from '../AuthContext'
import Modal from '../components/Modal'
import Avatar from '../components/Avatar'
import Loader from '../components/Loader'
import EmptyState from '../components/EmptyState'

// Statut -> texte et couleur de la pastille
export const STATUTS_SEANCE = {
  EN_ATTENTE: { libelle: 'En attente', classe: 'pill-warning' },
  CONFIRMEE: { libelle: 'Confirmée', classe: 'pill-success' },
  ANNULEE: { libelle: 'Annulée', classe: 'pill-danger' },
}
const DUREES = [30, 45, 60, 90]

// Date locale -> "AAAA-MM-JJ" (sans passer par l'UTC)
function versIso(d) {
  return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0')
}

function dateCourte(iso) {
  return new Date(iso + 'T00:00').toLocaleDateString('fr-FR', { weekday: 'short', day: 'numeric', month: 'short' })
}

function estPassee(s) {
  return new Date(s.dateSeance + 'T' + s.heure) < new Date()
}

// "Sami Trabelsi" -> { prenom: 'Sami', nom: 'Trabelsi' } pour l'avatar
function personne(nomComplet, photo) {
  const [prenom, ...reste] = nomComplet.split(' ')
  return { prenom, nom: reste.join(' '), photo }
}

// Coaching prive : l'adherent reserve, le coach confirme, l'admin voit tout
export default function Seances() {
  const { estAdmin, estCoach, estAdherent } = useAuth()
  const [seances, setSeances] = useState([])
  const [onglet, setOnglet] = useState('avenir')
  const [erreur, setErreur] = useState('')
  const [message, setMessage] = useState('')
  const [chargement, setChargement] = useState(true)

  const [coachs, setCoachs] = useState([])
  const [adherents, setAdherents] = useState([])
  const [reservation, setReservation] = useState(null) // formulaire ouvert
  const [aAnnuler, setAAnnuler] = useState(null)
  const [aSupprimer, setASupprimer] = useState(null)
  const [erreurForm, setErreurForm] = useState('')
  const [enCours, setEnCours] = useState(false)

  async function charger() {
    try {
      setSeances(await getSeances())
      setErreur('')
    } catch (err) {
      setErreur(err.message)
    } finally {
      setChargement(false)
    }
  }

  useEffect(() => {
    charger()
    if (!estCoach) getCoachs().then(setCoachs).catch(() => {})
    if (estAdmin) getAdherents().then(setAdherents).catch(() => {})
  }, [estAdmin, estCoach])

  // ---------- Reservation ----------

  function ouvrirReservation() {
    setErreurForm('')
    setMessage('')
    const demain = new Date()
    demain.setDate(demain.getDate() + 1)
    setReservation({ coachId: '', adherentId: '', dateSeance: versIso(demain), heure: '17:00', dureeMinutes: 60, objectif: '' })
  }

  function changer(e) {
    setReservation({ ...reservation, [e.target.name]: e.target.value })
  }

  async function reserver(e) {
    e.preventDefault()
    if (!reservation.coachId) {
      setErreurForm('Choisissez un coach.')
      return
    }
    setEnCours(true)
    try {
      await createSeance({
        coachId: Number(reservation.coachId),
        adherentId: estAdmin ? Number(reservation.adherentId) : null,
        dateSeance: reservation.dateSeance,
        heure: reservation.heure,
        dureeMinutes: Number(reservation.dureeMinutes),
        objectif: reservation.objectif,
      })
      setReservation(null)
      setOnglet('avenir')
      setMessage(estAdmin ? 'Séance réservée et confirmée.' : 'Demande envoyée. Le coach va la confirmer.')
      charger()
    } catch (err) {
      setErreurForm(err.message)
    } finally {
      setEnCours(false)
    }
  }

  // ---------- Confirmer / annuler / supprimer ----------

  async function changerStatut(seance, statut) {
    setEnCours(true)
    setErreur('')
    try {
      await changerStatutSeance(seance.id, statut)
      setAAnnuler(null)
      setMessage(statut === 'CONFIRMEE' ? 'Séance confirmée.' : 'Séance annulée.')
      charger()
    } catch (err) {
      setErreur(err.message)
      setAAnnuler(null)
    } finally {
      setEnCours(false)
    }
  }

  async function supprimer() {
    setEnCours(true)
    try {
      await deleteSeance(aSupprimer.id)
      setASupprimer(null)
      charger()
    } catch (err) {
      setErreur(err.message)
      setASupprimer(null)
    } finally {
      setEnCours(false)
    }
  }

  const avenir = seances.filter((s) => !estPassee(s))
  const historique = seances.filter(estPassee).reverse()
  const affichees = onglet === 'avenir' ? avenir : historique
  const enAttente = avenir.filter((s) => s.statut === 'EN_ATTENTE').length

  const titre = estAdmin ? 'Séances privées' : estCoach ? 'Mes séances' : 'Coaching privé'

  return (
    <>
      <div className="page-head">
        <div>
          <h1>{titre}</h1>
          <p className="muted">
            {avenir.length} séance(s) à venir
            {enAttente > 0 && (estCoach || estAdmin) && ', dont ' + enAttente + ' à confirmer'}
          </p>
        </div>
        <div className="page-head-actions">
          <div className="segmented" role="group" aria-label="Période">
            <button className={onglet === 'avenir' ? 'actif' : ''} onClick={() => setOnglet('avenir')}>À venir</button>
            <button className={onglet === 'passees' ? 'actif' : ''} onClick={() => setOnglet('passees')}>Historique</button>
          </div>
          {!estCoach && (
            <button className="btn btn-primary" onClick={ouvrirReservation}>
              <Plus size={16} aria-hidden="true" /> Réserver une séance
            </button>
          )}
        </div>
      </div>

      <p className="alert alert-info" style={{ marginBottom: 16 }}>
        <Lock size={16} aria-hidden="true" /> Les séances privées sont visibles uniquement par le coach et l'adhérent concernés.
      </p>
      {message && <p className="alert alert-success" style={{ marginBottom: 16 }}><CheckCircle2 size={16} aria-hidden="true" /> {message}</p>}
      {erreur && <p className="error" style={{ marginBottom: 16 }}>{erreur}</p>}
      {chargement && <Loader />}

      {!chargement && affichees.length === 0 && (
        <EmptyState icone={Handshake}
          titre={onglet === 'avenir' ? 'Aucune séance à venir.' : 'Aucune séance passée.'}>
          {estAdherent && onglet === 'avenir' && (
            <>
              <span>Un coach vous accompagne en tête-à-tête sur votre objectif.</span>
              <button className="btn btn-primary" onClick={ouvrirReservation}>Réserver une séance</button>
            </>
          )}
        </EmptyState>
      )}

      <div className="seances stagger" key={onglet}>
        {affichees.map((s, i) => {
          const statut = STATUTS_SEANCE[s.statut]
          const passee = estPassee(s)
          const actif = !passee && s.statut !== 'ANNULEE'
          return (
            <article key={s.id} className={'scard' + (s.statut === 'ANNULEE' ? ' annulee' : '')} style={{ '--i': i }}>
              <div className="scard-quand">
                <span className="scard-jour">{dateCourte(s.dateSeance)}</span>
                <span className="scard-heure">{s.heure.slice(0, 5)}</span>
                <span className="hint"><Clock size={12} aria-hidden="true" /> {s.dureeMinutes} min</span>
              </div>

              <div className="scard-corps">
                <div className="scard-personnes">
                  {!estCoach && (
                    <span className="ucard-head">
                      <Avatar personne={personne(s.coachNom, s.coachPhoto)} taille="sm" />
                      <span>
                        <span className="ucard-title">{s.coachNom}</span>
                        <span className="ucard-sub" style={{ display: 'block' }}>{s.coachSpecialite || 'Coach'}</span>
                      </span>
                    </span>
                  )}
                  {!estAdherent && (
                    <span className="ucard-head">
                      <Avatar personne={personne(s.adherentNom, s.adherentPhoto)} taille="sm" />
                      <span>
                        <span className="ucard-title">{s.adherentNom}</span>
                        <span className="ucard-sub" style={{ display: 'block' }}>Adhérent</span>
                      </span>
                    </span>
                  )}
                </div>
                {s.objectif && <p className="scard-objectif"><Target size={13} aria-hidden="true" /> {s.objectif}</p>}
              </div>

              <div className="scard-fin">
                <span className={'pill ' + statut.classe}>{statut.libelle}</span>
                <div className="ucard-actions">
                  {actif && (estCoach || estAdmin) && s.statut === 'EN_ATTENTE' && (
                    <button className="btn btn-small btn-primary" disabled={enCours} onClick={() => changerStatut(s, 'CONFIRMEE')}>
                      <Check size={14} aria-hidden="true" /> Confirmer
                    </button>
                  )}
                  {actif && (
                    <button className="btn btn-small" disabled={enCours} onClick={() => setAAnnuler(s)}>
                      <X size={14} aria-hidden="true" /> {estCoach && s.statut === 'EN_ATTENTE' ? 'Refuser' : 'Annuler'}
                    </button>
                  )}
                  {estAdmin && (
                    <button className="btn btn-small btn-danger-ghost" aria-label="Supprimer" onClick={() => setASupprimer(s)}>
                      <Trash2 size={14} aria-hidden="true" />
                    </button>
                  )}
                </div>
              </div>
            </article>
          )
        })}
      </div>

      {/* ---------- Reservation ---------- */}
      {reservation && (
        <Modal titre="Réserver une séance privée" onClose={() => setReservation(null)}>
          <form className="form" onSubmit={reserver}>
            {estAdmin && (
              <label>
                Adhérent
                <select name="adherentId" value={reservation.adherentId} onChange={changer} required>
                  <option value="">Choisir…</option>
                  {adherents.map((a) => <option key={a.id} value={a.id}>{a.prenom} {a.nom}</option>)}
                </select>
              </label>
            )}
            <fieldset className="choix-coach">
              <legend>Coach</legend>
              {coachs.map((c) => (
                <label key={c.id} className={'choix-coach-carte' + (String(reservation.coachId) === String(c.id) ? ' choisi' : '')}>
                  <input type="radio" name="coachId" value={c.id} checked={String(reservation.coachId) === String(c.id)} onChange={changer} />
                  <Avatar personne={c} taille="sm" />
                  <span>
                    <span className="ucard-title">{c.prenom} {c.nom}</span>
                    <span className="ucard-sub" style={{ display: 'block' }}>{c.specialite || 'Coach'}</span>
                  </span>
                </label>
              ))}
            </fieldset>
            <div className="row">
              <label>
                Date
                <input name="dateSeance" type="date" value={reservation.dateSeance} min={versIso(new Date())} onChange={changer} required />
              </label>
              <label>
                Heure
                <input name="heure" type="time" value={reservation.heure} onChange={changer} required />
              </label>
              <label>
                Durée
                <select name="dureeMinutes" value={reservation.dureeMinutes} onChange={changer}>
                  {DUREES.map((d) => <option key={d} value={d}>{d} min</option>)}
                </select>
              </label>
            </div>
            <label>
              Objectif <span className="hint">facultatif</span>
              <textarea name="objectif" value={reservation.objectif} onChange={changer} maxLength={255}
                placeholder="Ex. : préparer un 10 km, reprendre après une blessure…" />
            </label>
            {erreurForm && <p className="error">{erreurForm}</p>}
            <div className="modal-actions">
              <button type="button" className="btn" onClick={() => setReservation(null)}>Annuler</button>
              <button className="btn btn-primary" disabled={enCours}>{enCours ? 'Envoi…' : estAdmin ? 'Réserver' : 'Envoyer la demande'}</button>
            </div>
          </form>
        </Modal>
      )}

      {aAnnuler && (
        <Modal titre="Annuler la séance ?" onClose={() => setAAnnuler(null)}>
          <p>
            Séance du {dateCourte(aAnnuler.dateSeance)} à {aAnnuler.heure.slice(0, 5)}
            {estAdherent ? ' avec ' + aAnnuler.coachNom : ' avec ' + aAnnuler.adherentNom}.
            Le créneau sera libéré.
          </p>
          <div className="modal-actions">
            <button className="btn" onClick={() => setAAnnuler(null)}>Garder la séance</button>
            <button className="btn btn-danger" disabled={enCours} onClick={() => changerStatut(aAnnuler, 'ANNULEE')}>Annuler la séance</button>
          </div>
        </Modal>
      )}

      {aSupprimer && (
        <Modal titre="Supprimer la séance ?" onClose={() => setASupprimer(null)}>
          <p>La séance du {dateCourte(aSupprimer.dateSeance)} ({aSupprimer.coachNom} avec {aSupprimer.adherentNom}) sera supprimée définitivement.</p>
          <div className="modal-actions">
            <button className="btn" onClick={() => setASupprimer(null)}>Annuler</button>
            <button className="btn btn-danger" disabled={enCours} onClick={supprimer}>Supprimer</button>
          </div>
        </Modal>
      )}
    </>
  )
}
