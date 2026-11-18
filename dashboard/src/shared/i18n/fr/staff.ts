export const frStaff = {
  staff: {
    title: "Personnel NDZela",
    subtitle: "L'énergie consommée par le personnel NDZela. Ces recharges sont offertes : rien n'est débité ni encaissé.",
    period: "Période",
    periods: { today: "Aujourd'hui", week: "7 derniers jours", month: "30 derniers jours", all: "Tout" },
    loadError: "Impossible de charger la consommation du personnel.",
    energy: "Énergie consommée",
    energyHint: "Recharges terminées ou interrompues",
    value: "Valeur offerte",
    valueHint: "Au tarif normal, jamais encaissée",
    charges: "Recharges",
    chargesHint: "Recharges du personnel",
    empty: "Aucune recharge du personnel sur cette période.",
    columns: { member: "Membre", charges: "Recharges", energy: "Énergie", value: "Valeur offerte", last: "Dernière recharge" },
  },
} as const;
