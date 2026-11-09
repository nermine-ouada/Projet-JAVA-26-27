import { Link } from 'react-router-dom'
// Mise en page commune a la connexion et a l'inscription
export default function AuthShell({ children }) {
  return (
    <div className="auth">
      <aside className="auth-side">
        <Link to="/" className="brand brand-lg">Club Atlas</Link>
        <p>Planning des cours, salles et adhérents.<br />Tout le club au même endroit.</p>
        <span className="auth-foot">ENICarthage · Projet fédérateur</span>
      </aside>
      <section className="auth-main">
        <div className="auth-card">{children}</div>
      </section>
    </div>
  )
}
