// Fenetre simple au-dessus de la page (formulaires, confirmation de suppression)
export default function Modal({ titre, onClose, children }) {
  return (
    <div className="overlay" onMouseDown={onClose}>
      <div className="modal" onMouseDown={(e) => e.stopPropagation()}>
        <div className="modal-head">
          <h2>{titre}</h2>
          <button className="btn-close" onClick={onClose} aria-label="Fermer">×</button>
        </div>
        {children}
      </div>
    </div>
  )
}
