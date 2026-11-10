// Etat vide : dit ce qui manque et propose une action
export default function EmptyState({ icone: Icone, titre, children }) {
  return (
    <div className="empty-state">
      {Icone && <Icone size={36} strokeWidth={1.5} />}
      <strong>{titre}</strong>
      {children}
    </div>
  )
}
