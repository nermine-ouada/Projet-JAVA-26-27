import { Link } from 'react-router-dom'
// Mise en page de la page de connexion
export default function AuthShell({ children }) {
  return (
    <div className="auth">
      <aside className="auth-side">
        <Link to="/" className="brand brand-lg">Club Atlas</Link>
        <p>Réservez.<br />Entraînez-vous.<br />Progressez.</p>
        <span className="auth-foot">ENICarthage · Projet fédérateur</span>
      </aside>
      <section className="auth-main">
        <div className="auth-card">{children}</div>
      </section>
    </div>
  )
}
