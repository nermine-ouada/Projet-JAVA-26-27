import { useEffect } from 'react'
import { createPortal } from 'react-dom'

// Fenetre au-dessus de la page (formulaires, confirmation de suppression).
// Se ferme avec la croix, la touche Echap ou un clic a cote.
// Rendue directement dans <body> (portail) : le fond gris couvre tout l'ecran,
// meme si la page parente est animee.
export default function Modal({ titre, onClose, children }) {
  useEffect(() => {
    function touche(e) {
      if (e.key === 'Escape') onClose()
    }
    document.addEventListener('keydown', touche)
    return () => document.removeEventListener('keydown', touche)
  }, [onClose])

  return createPortal(
    <div className="overlay" onMouseDown={onClose}>
      <div className="modal" role="dialog" aria-modal="true" aria-label={titre} onMouseDown={(e) => e.stopPropagation()}>
        <div className="modal-head">
          <h2>{titre}</h2>
          <button className="btn-close" onClick={onClose} aria-label="Fermer">×</button>
        </div>
        {children}
      </div>
    </div>,
    document.body
  )
}
