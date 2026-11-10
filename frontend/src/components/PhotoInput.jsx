import { useRef, useState } from 'react'
import { Camera, Trash2 } from 'lucide-react'
import Avatar from './Avatar'

const TAILLE = 256 // la photo est recadree en carre de 256 x 256 px avant l'envoi

// Lit le fichier choisi, le recadre au centre et le reduit, puis renvoie
// une image JPEG en texte ("data:image/jpeg;base64,...") que l'API stocke telle quelle.
function reduireImage(fichier) {
  return new Promise((resolve, reject) => {
    const lecteur = new FileReader()
    lecteur.onerror = () => reject(new Error('Impossible de lire ce fichier.'))
    lecteur.onload = () => {
      const img = new Image()
      img.onerror = () => reject(new Error('Ce fichier n\'est pas une image valide.'))
      img.onload = () => {
        const cote = Math.min(img.width, img.height)
        const canvas = document.createElement('canvas')
        canvas.width = TAILLE
        canvas.height = TAILLE
        canvas.getContext('2d').drawImage(
          img,
          (img.width - cote) / 2, (img.height - cote) / 2, cote, cote, // partie centrale de la photo
          0, 0, TAILLE, TAILLE,
        )
        resolve(canvas.toDataURL('image/jpeg', 0.85))
      }
      img.src = lecteur.result
    }
    lecteur.readAsDataURL(fichier)
  })
}

// Choix d'une photo avec apercu. valeur = photo actuelle (ou null), onChange(nouvellePhoto ou "")
export default function PhotoInput({ personne, valeur, onChange, taille = 'lg' }) {
  const champ = useRef(null)
  const [erreur, setErreur] = useState('')

  async function choisir(e) {
    const fichier = e.target.files[0]
    e.target.value = '' // permet de rechoisir le meme fichier
    if (!fichier) return
    if (!fichier.type.startsWith('image/')) {
      setErreur('Choisissez une image (jpg, png ou webp).')
      return
    }
    try {
      setErreur('')
      onChange(await reduireImage(fichier))
    } catch (err) {
      setErreur(err.message)
    }
  }

  return (
    <div className="photo-input">
      <Avatar personne={{ ...personne, photo: valeur }} taille={taille} />
      <div className="photo-input-actions">
        <input ref={champ} type="file" accept="image/*" onChange={choisir} />
        <button type="button" className="btn btn-small" onClick={() => champ.current.click()}>
          <Camera size={14} aria-hidden="true" /> {valeur ? 'Changer la photo' : 'Ajouter une photo'}
        </button>
        {valeur && (
          <button type="button" className="btn btn-small btn-ghost" onClick={() => onChange('')}>
            <Trash2 size={14} aria-hidden="true" /> Retirer
          </button>
        )}
        {erreur && <span className="hint" style={{ color: 'var(--danger)' }}>{erreur}</span>}
      </div>
    </div>
  )
}
