import { useState } from 'react'
import { CheckCircle2 } from 'lucide-react'
import { useAuth } from '../AuthContext'
import { updateMaPhoto } from '../api'
import { ROLES } from '../roles'
import PhotoInput from '../components/PhotoInput'
// [nermine] imports


// Informations du compte connecte (visible par tous les roles)
export default function Profil() {
  const { utilisateur, majUtilisateur } = useAuth()
  const [message, setMessage] = useState('')
  const [erreur, setErreur] = useState('')

  // La photo est enregistree des qu'elle est choisie
  async function changerPhoto(photo) {
    setMessage('')
    setErreur('')
    try {
      majUtilisateur(await updateMaPhoto(photo))
      setMessage(photo ? 'Photo enregistrée.' : 'Photo retirée.')
    } catch (err) {
      setErreur(err.message)
    }
  }

  return (
    <>
      <div className="page-head">
        <div>
          <h1>Mon profil</h1>
          <p className="muted">Informations de votre compte</p>
        </div>
      </div>

      <div className="profil-head">
        <PhotoInput personne={utilisateur} valeur={utilisateur.photo} onChange={changerPhoto} />
        <div>
          <h2>{utilisateur.prenom} {utilisateur.nom}</h2>
          <span className={'badge badge-' + utilisateur.role.toLowerCase()}>{ROLES[utilisateur.role]}</span>
        </div>
      </div>
      {message && <p className="alert alert-success" style={{ maxWidth: 560, marginBottom: 16 }}><CheckCircle2 size={16} aria-hidden="true" /> {message}</p>}
      {erreur && <p className="error" style={{ maxWidth: 560, marginBottom: 16 }}>{erreur}</p>}

      <div className="card profil">
        <dl>
          <dt>Prénom</dt>
          <dd>{utilisateur.prenom}</dd>
          <dt>Nom</dt>
          <dd>{utilisateur.nom}</dd>
          <dt>Email</dt>
          <dd>{utilisateur.email}</dd>
          <dt>Téléphone</dt>
          <dd>{utilisateur.telephone || <span className="muted">Non renseigné</span>}</dd>
          {utilisateur.role === 'COACH' && (
            <>
              <dt>Spécialité</dt>
              <dd>{utilisateur.specialite || <span className="muted">Non renseignée</span>}</dd>
            </>
          )}
        </dl>
      </div>

      {/* [nermine] sections */}

    </>
  )
}
