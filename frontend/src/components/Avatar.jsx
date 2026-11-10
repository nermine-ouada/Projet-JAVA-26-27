// Photo de la personne, ou ses initiales si elle n'a pas de photo ("?" si le nom est vide)
export default function Avatar({ personne, taille = '', className = '' }) {
  const premiere = (texte) => (texte || '').trim().charAt(0)
  const initiales = (premiere(personne?.prenom) + premiere(personne?.nom)) || '?'
  const classes = ['avatar', taille && 'avatar-' + taille, className].filter(Boolean).join(' ')
  return (
    <span className={classes} aria-hidden="true">
      {personne?.photo ? <img src={personne.photo} alt="" /> : initiales}
    </span>
  )
}
