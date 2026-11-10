import { useEffect, useState } from 'react'

// Clair (par defaut) -> Sombre -> Auto (suit le systeme) -> Clair
const MODES = [
  { valeur: 'light', libelle: 'Clair' },
  { valeur: 'dark', libelle: 'Sombre' },
  { valeur: 'auto', libelle: 'Auto' },
]
const CLE = 'club-atlas-theme' // meme cle que le script de index.html

function lireTheme() {
  try {
    return localStorage.getItem(CLE) || 'light'
  } catch {
    return 'light'
  }
}

export default function ThemeToggle() {
  const [theme, setTheme] = useState(lireTheme)

  // Pose data-theme sur <html> : les variables CSS changent de valeur
  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme)
    try {
      localStorage.setItem(CLE, theme)
    } catch {
      // stockage indisponible : le choix vaut pour cette page seulement
    }
  }, [theme])

  const index = Math.max(0, MODES.findIndex((m) => m.valeur === theme))
  const suivant = MODES[(index + 1) % MODES.length]

  return (
    <button type="button" className="theme-toggle" onClick={() => setTheme(suivant.valeur)}
      title={'Passer en mode ' + suivant.libelle.toLowerCase()}>
      Thème : {MODES[index].libelle}
    </button>
  )
}
