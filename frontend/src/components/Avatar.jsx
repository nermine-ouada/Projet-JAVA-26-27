// Photo de la personne, ou ses initiales si elle n'a pas de photo
export default function Avatar({ personne, taille = '', className = '' }) {
  const initiales = ((personne?.prenom || '?')[0] + (personne?.nom || '')[0]).trim()
  const classes = ['avatar', taille && 'avatar-' + taille, className].filter(Boolean).join(' ')
  return (
    <span className={classes} aria-hidden="true">
      {personne?.photo ? <img src={personne.photo} alt="" /> : initiales}
    </span>
  )
}
