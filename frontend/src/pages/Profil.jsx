import { useAuth } from '../AuthContext'
import { ROLES } from './Utilisateurs'

// Informations du compte connecté (visible par tous les rôles)
export default function Profil() {
  const { utilisateur } = useAuth()

  return (
    <>
      <div className="page-head">
        <div>
          <h1>Mon profil</h1>
          <p className="muted">Informations de votre compte</p>
        </div>
      </div>

      <div className="card profil">
        <dl>
          <dt>Prénom</dt>
          <dd>{utilisateur.prenom}</dd>
          <dt>Nom</dt>
          <dd>{utilisateur.nom}</dd>
          <dt>Email</dt>
          <dd>{utilisateur.email}</dd>
          <dt>Rôle</dt>
          <dd><span className={'badge badge-' + utilisateur.role.toLowerCase()}>{ROLES[utilisateur.role]}</span></dd>
        </dl>
      </div>
    </>
  )
}
