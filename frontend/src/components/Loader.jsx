// Indicateur de chargement : disques de fonte qui montent et descendent
export default function Loader({ texte = 'Chargement…' }) {
  return (
    <div className="loader" role="status">
      <div className="loader-plates" aria-hidden="true">
        <span /><span /><span /><span /><span />
      </div>
      {texte}
    </div>
  )
}
