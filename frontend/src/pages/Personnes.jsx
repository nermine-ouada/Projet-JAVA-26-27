import { useEffect, useState } from 'react'
import { Search, Dumbbell, Users } from 'lucide-react'
import { getUtilisateurs, createUtilisateur, updateUtilisateur, deleteUtilisateur } from '../api'
import Modal from '../components/Modal'
import Avatar from '../components/Avatar'
import PhotoInput from '../components/PhotoInput'
import Loader from '../components/Loader'
import EmptyState from '../components/EmptyState'

// Textes de la page selon le role affiche
const CONFIG = {
  COACH: { titre: 'Coachs', un: 'un coach', ajouter: 'Ajouter un coach', icone: Dumbbell },
  ADHERENT: { titre: 'Adhérents', un: 'un adhérent', ajouter: 'Ajouter un adhérent', icone: Users },
}

const VIDE = { nom: '', prenom: '', email: '', motDePasse: '', telephone: '', specialite: '', photo: '' }

// CRUD des coachs ou des adherents (admin). Utilise l'API /utilisateurs, filtree par role.
export default function Personnes({ role }) {
  const config = CONFIG[role]
  const [personnes, setPersonnes] = useState([])
  const [recherche, setRecherche] = useState('')
  const [erreur, setErreur] = useState('')
  const [chargement, setChargement] = useState(true)

  const [edition, setEdition] = useState(null)      // null = ferme, sinon le formulaire
  const [photoModifiee, setPhotoModifiee] = useState(false)
  const [aSupprimer, setASupprimer] = useState(null)
  const [erreurForm, setErreurForm] = useState('')
  const [enCours, setEnCours] = useState(false)

  async function charger() {
    try {
      const tous = await getUtilisateurs()
      setPersonnes(tous.filter((u) => u.role === role))
      setErreur('')
    } catch (err) {
      setErreur(err.message)
    } finally {
      setChargement(false)
    }
  }

  useEffect(() => {
    setChargement(true)
    charger()
  }, [role])

  function ouvrir(p) {
    setErreurForm('')
    setPhotoModifiee(false)
    setEdition(p ? { ...VIDE, ...p, motDePasse: '', telephone: p.telephone || '', specialite: p.specialite || '', photo: p.photo || '' } : VIDE)
  }

  function changer(e) {
    setEdition({ ...edition, [e.target.name]: e.target.value })
  }

  async function enregistrer(e) {
    e.preventDefault()
    setEnCours(true)
    const corps = {
      nom: edition.nom,
      prenom: edition.prenom,
      email: edition.email,
      motDePasse: edition.motDePasse,
      role,
      telephone: edition.telephone,
      specialite: role === 'COACH' ? edition.specialite : '',
      // En modification, on n'envoie la photo que si elle a change (null = inchangee)
      photo: edition.id && !photoModifiee ? null : edition.photo,
    }
    try {
      if (edition.id) await updateUtilisateur(edition.id, corps)
      else await createUtilisateur(corps)
      setEdition(null)
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
      await deleteUtilisateur(aSupprimer.id)
      setASupprimer(null)
      charger()
    } catch (err) {
      setErreurForm(err.message)
    } finally {
      setEnCours(false)
    }
  }

  const texte = recherche.trim().toLowerCase()
  const affichees = personnes.filter((p) =>
    [p.prenom, p.nom, p.email, p.specialite].join(' ').toLowerCase().includes(texte))

  return (
    <>
      <div className="page-head">
        <div>
          <h1>{config.titre}</h1>
          <p className="muted">{personnes.length} {config.titre.toLowerCase()} inscrit(s)</p>
        </div>
        <div className="page-head-actions">
          <label className="search">
            <Search size={16} aria-hidden="true" />
            <input type="search" placeholder="Rechercher…" value={recherche}
              onChange={(e) => setRecherche(e.target.value)} aria-label="Rechercher" />
          </label>
          <button className="btn btn-primary" onClick={() => ouvrir(null)}>
            {config.ajouter}
          </button>
        </div>
      </div>

      {erreur && <p className="error">{erreur}</p>}
      {chargement && <Loader />}

      {!chargement && personnes.length === 0 && (
        <EmptyState icone={config.icone} titre={'Aucun ' + config.titre.toLowerCase().slice(0, -1) + ' pour le moment.'}>
          <button className="btn btn-primary" onClick={() => ouvrir(null)}>{config.ajouter}</button>
        </EmptyState>
      )}
      {!chargement && personnes.length > 0 && affichees.length === 0 && (
        <EmptyState icone={Search} titre="Aucun résultat.">Essayez un autre nom ou un autre email.</EmptyState>
      )}

      {/* L'effectif : un tableau, comme la liste affichee a l'accueil d'une salle */}
      {affichees.length > 0 && (
        <div className="card">
          <table className="table">
            <thead>
              <tr>
                <th>{role === 'COACH' ? 'Coach' : 'Adhérent'}</th>
                {role === 'COACH' && <th>Spécialité</th>}
                <th className="hide-sm">Téléphone</th>
                <th className="actions"></th>
              </tr>
            </thead>
            <tbody className="stagger">
              {affichees.map((p, i) => (
                <tr key={p.id} style={{ '--i': i }}>
                  <td>
                    <div className="personne-cell">
                      <Avatar personne={p} />
                      <div>
                        <div className="personne-nom">{p.prenom} {p.nom}</div>
                        <div className="personne-email">{p.email}</div>
                      </div>
                    </div>
                  </td>
                  {role === 'COACH' && <td>{p.specialite || <span className="muted">Non renseignée</span>}</td>}
                  <td className="hide-sm">{p.telephone || <span className="muted">Non renseigné</span>}</td>
                  <td className="actions">
                    <button className="btn btn-small" onClick={() => ouvrir(p)} aria-label={'Modifier ' + p.prenom}>Modifier</button>
                    <button className="btn btn-small btn-danger-ghost" aria-label={'Supprimer ' + p.prenom}
                      onClick={() => { setErreurForm(''); setASupprimer(p) }}>
                      Supprimer
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {edition && (
        <Modal titre={edition.id ? 'Modifier ' + config.un : config.ajouter} onClose={() => setEdition(null)}>
          <form className="form" onSubmit={enregistrer}>
            <PhotoInput personne={edition} valeur={edition.photo}
              onChange={(photo) => { setEdition({ ...edition, photo }); setPhotoModifiee(true) }} />
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
            <div className="row">
              <label>
                Téléphone <span className="hint">facultatif</span>
                <input name="telephone" type="tel" value={edition.telephone} onChange={changer} placeholder="22 000 000" />
              </label>
              {role === 'COACH' && (
                <label>
                  Spécialité
                  <input name="specialite" value={edition.specialite} onChange={changer} placeholder="Musculation, yoga…" />
                </label>
              )}
            </div>
            <label>
              Mot de passe{' '}
              <span className="hint">{edition.id ? 'laisser vide pour ne pas le changer' : '6 caractères minimum'}</span>
              <input name="motDePasse" type="password" minLength={6} value={edition.motDePasse}
                onChange={changer} required={!edition.id} />
            </label>
            {erreurForm && <p className="error">{erreurForm}</p>}
            <div className="modal-actions">
              <button type="button" className="btn" onClick={() => setEdition(null)}>Annuler</button>
              <button className="btn btn-primary" disabled={enCours}>{enCours ? 'Enregistrement…' : 'Enregistrer'}</button>
            </div>
          </form>
        </Modal>
      )}

      {aSupprimer && (
        <Modal titre={'Supprimer ' + config.un + ' ?'} onClose={() => setASupprimer(null)}>
          <div className="ucard-head" style={{ marginTop: 14 }}>
            <Avatar personne={aSupprimer} />
            <div>
              <div className="ucard-title">{aSupprimer.prenom} {aSupprimer.nom}</div>
              <div className="ucard-sub">{aSupprimer.email}</div>
            </div>
          </div>
          <p>Le compte sera supprimé définitivement, avec ses abonnements et ses inscriptions.
            {role === 'COACH' && ' Un coach qui anime encore des cours ne peut pas être supprimé.'}</p>
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
