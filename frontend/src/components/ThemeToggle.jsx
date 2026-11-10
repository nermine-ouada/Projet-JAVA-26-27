import { useEffect, useState } from 'react'
import { Monitor, Sun, Moon } from 'lucide-react'

// Auto -> Clair -> Sombre -> Auto. "Auto" suit le reglage du systeme.
const MODES = [
  { valeur: 'auto', libelle: 'Auto', icone: Monitor },
  { valeur: 'light', libelle: 'Clair', icone: Sun },
  { valeur: 'dark', libelle: 'Sombre', icone: Moon },
]
const CLE = 'club-atlas-theme' // meme cle que le script de index.html

function lireTheme() {
  try {
    return localStorage.getItem(CLE) || 'auto'
  } catch {
    return 'auto'
  }
}

export default function ThemeToggle() {
  const [theme, setTheme] = useState(lireTheme)

  // Pose data-theme sur <html> : les variables CSS changent de valeur
  useEffect(() => {
    if (theme === 'auto') document.documentElement.removeAttribute('data-theme')
    else document.documentElement.setAttribute('data-theme', theme)
    try {
      localStorage.setItem(CLE, theme)
    } catch {
      // stockage indisponible : le choix vaut pour cette page seulement
    }
  }, [theme])

  const index = MODES.findIndex((m) => m.valeur === theme)
  const mode = MODES[index === -1 ? 0 : index]
  const suivant = MODES[(index + 1) % MODES.length]
  const Icone = mode.icone

  return (
    <button type="button" className="theme-toggle" onClick={() => setTheme(suivant.valeur)}
      title={'Thème : ' + mode.libelle + ' (cliquer pour ' + suivant.libelle + ')'}>
      <Icone size={14} aria-hidden="true" /> <span>{mode.libelle}</span>
    </button>
  )
}
