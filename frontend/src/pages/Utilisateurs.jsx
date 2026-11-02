import { useEffect, useState } from 'react'
import { getUtilisateurs, createUtilisateur, updateUtilisateur, deleteUtilisateur } from '../api'
import { useAuth } from '../AuthContext'
import Modal from '../components/Modal'

export const ROLES = { ADMIN: 'Admin', COACH: 'Coach', ADHERENT: 'Adhérent' }

const VIDE = { nom: '', prenom: '', email: '', motDePasse: '', role: 'ADHERENT' }

// CRUD des utilisateurs (page réservée à l'admin)
export default function Utilisateurs() {
  const { utilisateur: moi } = useAuth()
  const [utilisateurs, setUtilisateurs] = useState([])
  const [erreur, setErreur] = useState('')
  const [chargement, setChargement] = useState(true)

  const [edition, setEdition] = useState(null)      // null = fermé, sinon le formulaire
  const [aSupprimer, setASupprimer] = useState(null) // utilisateur à confirmer
  const [erreurForm, setErreurForm] = useState('')

  async function charger() {
    try {
      setUtilisateurs(await getUtilisateurs())
      setErreur('')
    } catch (err) {
      setErreur(err.message)
    } finally {
      setChargement(false)
    }
  }

  useEffect(() => { charger() }, [])

  function ouvrir(u) {
    setErreurForm('')
    // En modification, le mot de passe reste vide (= inchangé)
    setEdition(u ? { ...u, motDePasse: '' } : VIDE)
  }

  function changer(e) {
    setEdition({ ...edition, [e.target.name]: e.target.value })
  }

  async function enregistrer(e) {
    e.preventDefault()
    try {
      if (edition.id) await updateUtilisateur(edition.id, edition)
      else await createUtilisateur(edition)
      setEdition(null)
      charger()
    } catch (err) {
      setErreurForm(err.message)
    }
  }

  async function supprimer() {
    try {
      await deleteUtilisateur(aSupprimer.id)
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
          <h1>Utilisateurs</h1>
          <p className="muted">{utilisateurs.length} compte(s)</p>
        </div>
        <button className="btn btn-primary" onClick={() => ouvrir(null)}>Nouvel utilisateur</button>
      </div>

      {erreur && <p className="error">{erreur}</p>}

      <div className="card">
        <table className="table">
          <thead>
            <tr>
              <th>Nom</th>
              <th>Email</th>
              <th>Rôle</th>
              <th className="actions"></th>
            </tr>
          </thead>
          <tbody>
            {chargement && <tr><td colSpan="4" className="empty">Chargement…</td></tr>}
            {!chargement && utilisateurs.length === 0 && (
              <tr><td colSpan="4" className="empty">Aucun utilisateur.</td></tr>
            )}
            {utilisateurs.map((u) => (
              <tr key={u.id}>
                <td className="strong">{u.prenom} {u.nom}</td>
                <td>{u.email}</td>
                <td><span className={'badge badge-' + u.role.toLowerCase()}>{ROLES[u.role]}</span></td>
                <td className="actions">
                  <button className="btn btn-small" onClick={() => ouvrir(u)}>Modifier</button>
                  {u.id !== moi.id && (
                    <button className="btn btn-small btn-danger-ghost"
                      onClick={() => { setErreurForm(''); setASupprimer(u) }}>
                      Supprimer
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {edition && (
        <Modal titre={edition.id ? "Modifier l'utilisateur" : 'Nouvel utilisateur'} onClose={() => setEdition(null)}>
          <form className="form" onSubmit={enregistrer}>
            <div className="row">
              <label>
                Prénom
                <input name="prenom" value={edition.prenom} onChange={changer} required autoFocus />
              </label>
              <label>
                Nom
                <input name="nom" value={edition.nom} onChange={changer} required />
              </label>
            </div>
            <label>
              Email
              <input name="email" type="email" value={edition.email} onChange={changer} required />
            </label>
            <label>
              Mot de passe{' '}
              <span className="hint">{edition.id ? 'laisser vide pour ne pas le changer' : '6 caractères minimum'}</span>
              <input name="motDePasse" type="password" minLength={6} value={edition.motDePasse}
                onChange={changer} required={!edition.id} />
            </label>
            <label>
              Rôle {edition.id && <span className="hint">non modifiable</span>}
              <select name="role" value={edition.role} onChange={changer} disabled={!!edition.id}>
                {Object.entries(ROLES).map(([valeur, libelle]) => (
                  <option key={valeur} value={valeur}>{libelle}</option>
                ))}
              </select>
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
        <Modal titre="Supprimer l'utilisateur" onClose={() => setASupprimer(null)}>
          <p>
            Supprimer le compte de <strong>{aSupprimer.prenom} {aSupprimer.nom}</strong> ?
            Cette action est définitive.
          </p>
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
