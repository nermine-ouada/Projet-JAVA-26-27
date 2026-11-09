import { Link } from 'react-router-dom'
import {
  Dumbbell, HeartPulse, Flame, Flower2, Award, CalendarDays, Ticket, TrendingUp,
  Clock, MapPin, Phone, Mail, ArrowRight,
} from 'lucide-react'
import { useAuth } from '../AuthContext'

// Contenu statique de la page d'accueil (modifiable ici sans toucher au HTML)
const ACTIVITES = [
  { titre: 'Musculation', image: '/images/musculation.jpg', icone: Dumbbell,
    texte: 'Plateau complet, haltères jusqu’à 50 kg et machines guidées.' },
  { titre: 'Cardio', image: '/images/cardio.jpg', icone: HeartPulse,
    texte: 'Tapis, vélos et rameurs pour travailler l’endurance à votre rythme.' },
  { titre: 'CrossFit', image: '/images/crossfit.jpg', icone: Flame,
    texte: 'Séances courtes et intenses en petit groupe, encadrées par un coach.' },
  { titre: 'Yoga & Pilates', image: '/images/yoga.jpg', icone: Flower2,
    texte: 'Souplesse, respiration et renforcement profond, le soir en semaine.' },
]

const AVANTAGES = [
  { icone: Award, titre: 'Coachs diplômés', texte: 'Une équipe qui suit votre progression séance après séance.' },
  { icone: CalendarDays, titre: 'Planning en ligne', texte: 'Consultez les cours de la semaine depuis votre espace.' },
  { icone: Ticket, titre: 'Abonnements flexibles', texte: 'Mensuel, trimestriel ou annuel, sans frais cachés.' },
  { icone: TrendingUp, titre: 'Suivi personnalisé', texte: 'Un programme adapté à votre niveau et à vos objectifs.' },
]

const HORAIRES = [
  ['Lundi – Vendredi', '06:00 – 22:00'],
  ['Samedi', '08:00 – 20:00'],
  ['Dimanche', '09:00 – 14:00'],
]

export default function Accueil() {
  const { utilisateur } = useAuth()

  return (
    <div className="home">
      {/* ---------- En-tête ---------- */}
      <header className="home-header">
        <div className="home-container home-header-inner">
          <Link to="/" className="brand brand-icon"><Dumbbell size={22} /> Club Atlas</Link>
          <nav className="home-nav">
            <a href="#activites">Activités</a>
            <a href="#avantages">Le club</a>
            <a href="#contact">Horaires & contact</a>
          </nav>
          <div className="home-actions">
            {utilisateur ? (
              <Link to="/profil" className="btn btn-primary">Mon espace</Link>
            ) : (
              <>
                <Link to="/login" className="btn btn-ghost-light">Se connecter</Link>
                <Link to="/register" className="btn btn-primary">S’inscrire</Link>
              </>
            )}
          </div>
        </div>
      </header>

      {/* ---------- Bandeau principal ---------- */}
      <section className="hero" style={{ backgroundImage: 'url(/images/hero.jpg)' }}>
        <div className="home-container hero-content">
          <span className="eyebrow">Salle de sport · Carthage</span>
          <h1>Entraînez-vous.<br />Progressez.<br />Ensemble.</h1>
          <p>Musculation, cardio et cours collectifs encadrés par des coachs diplômés,
            7 jours sur 7.</p>
          <div className="hero-buttons">
            <Link to={utilisateur ? '/profil' : '/register'} className="btn btn-primary btn-lg">
              {utilisateur ? 'Accéder à mon espace' : 'Créer mon compte'} <ArrowRight size={18} />
            </Link>
            <a href="#activites" className="btn btn-ghost-light btn-lg">Voir les activités</a>
          </div>
        </div>
      </section>

      <section className="stats">
        <div className="home-container stats-grid">
          <div><strong>3</strong><span>salles équipées</span></div>
          <div><strong>30+</strong><span>cours par semaine</span></div>
          <div><strong>7j/7</strong><span>ouvert toute l’année</span></div>
          <div><strong>800 m²</strong><span>d’espace d’entraînement</span></div>
        </div>
      </section>

      {/* ---------- Activités ---------- */}
      <section id="activites" className="home-section">
        <div className="home-container">
          <h2 className="section-title">Nos activités</h2>
          <div className="activites">
            {ACTIVITES.map(({ titre, image, icone: Icone, texte }) => (
              <article key={titre} className="activite">
                <img src={image} alt={titre} loading="lazy" />
                <div className="activite-body">
                  <h3><Icone size={18} /> {titre}</h3>
                  <p>{texte}</p>
                </div>
              </article>
            ))}
          </div>
        </div>
      </section>

      {/* ---------- Avantages ---------- */}
      <section id="avantages" className="home-section home-section-alt">
        <div className="home-container">
          <h2 className="section-title">Pourquoi Club Atlas</h2>
          <div className="avantages">
            {AVANTAGES.map(({ icone: Icone, titre, texte }) => (
              <div key={titre} className="avantage">
                <span className="avantage-icone"><Icone size={22} /></span>
                <h3>{titre}</h3>
                <p>{texte}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* ---------- Coaching ---------- */}
      <section className="coaching">
        <img src="/images/coach.jpg" alt="Séance de renforcement" loading="lazy" />
        <div className="coaching-text">
          <h2 className="section-title">Un coach à vos côtés</h2>
          <p>Bilan de départ, programme sur mesure et points réguliers :
            nos coachs vous accompagnent du premier jour jusqu’à vos objectifs.</p>
          <Link to={utilisateur ? '/profil' : '/register'} className="btn btn-primary">
            Commencer maintenant <ArrowRight size={16} />
          </Link>
        </div>
      </section>

      {/* ---------- Horaires & contact ---------- */}
      <section id="contact" className="home-section">
        <div className="home-container contact">
          <div>
            <h2 className="section-title"><Clock size={22} /> Horaires</h2>
            <table className="horaires">
              <tbody>
                {HORAIRES.map(([jours, heures]) => (
                  <tr key={jours}><td>{jours}</td><td className="num">{heures}</td></tr>
                ))}
              </tbody>
            </table>
          </div>
          <div>
            <h2 className="section-title">Contact</h2>
            <ul className="contact-list">
              <li><MapPin size={18} /> Avenue Habib Bourguiba, Carthage</li>
              <li><Phone size={18} /> +216 71 000 000</li>
              <li><Mail size={18} /> contact@clubatlas.tn</li>
            </ul>
          </div>
        </div>
      </section>

      <footer className="home-footer">
        <div className="home-container home-footer-inner">
          <span className="brand brand-icon"><Dumbbell size={18} /> Club Atlas</span>
          <span>ENICarthage · Projet fédérateur 2026-2027</span>
          <span>Photos : Unsplash</span>
        </div>
      </footer>
    </div>
  )
}
