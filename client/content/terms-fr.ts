import type { LegalDocument } from "./legal-types";

export const termsFr: LegalDocument = {
  title: "Conditions d’utilisation",
  intro:
    "En créant un compte ou en utilisant Zella Charging, vous acceptez les conditions ci-dessous. Lisez-les attentivement.",
  updatedLabel: "Dernière mise à jour",
  sections: [
    {
      heading: "1. Le service",
      paragraphs: ["Zella Charging est une application qui permet de trouver des bornes de recharge pour véhicules électriques, de démarrer et suivre une recharge et de payer avec un portefeuille alimenté par Mobile Money. Les bornes sont exploitées par un réseau partenaire."],
    },
    {
      heading: "2. Votre compte",
      bullets: [
        "Vous devez avoir 18 ans ou plus et fournir des informations exactes.",
        "Vous êtes responsable de la confidentialité de votre mot de passe et de l’activité de votre compte.",
        "Nous pouvons suspendre un compte en cas de fraude, d’usage abusif ou de non-respect de ces conditions.",
      ],
    },
    {
      heading: "3. Portefeuille et paiements",
      bullets: [
        "Vous créditez votre portefeuille avec Airtel Money ou Moov Money. Le montant crédité est disponible une fois le paiement confirmé par l’opérateur.",
        "Chaque recharge est débitée du portefeuille. Le tarif et les frais éventuels sont affichés avant le démarrage.",
        "Le solde du portefeuille n’est pas un compte bancaire et ne produit pas d’intérêts.",
      ],
    },
    {
      heading: "4. Retraits",
      bullets: [
        "Vous pouvez retirer votre solde disponible uniquement vers un numéro Mobile Money déjà utilisé pour créditer votre portefeuille.",
        "Le montant minimum d’un retrait est de 500 FCFA.",
        "Chaque retrait est examiné et validé par notre équipe avant l’envoi. En cas de refus, le montant est remis dans votre portefeuille.",
      ],
    },
    {
      heading: "5. Recharger en sécurité",
      bullets: [
        "Vérifiez que le connecteur est compatible avec votre véhicule avant de démarrer.",
        "Vous restez responsable de votre véhicule et de la zone de recharge pendant toute la durée de la recharge.",
        "Vous pouvez arrêter une recharge à tout moment depuis l’application ou à la borne.",
      ],
    },
    {
      heading: "6. Usages interdits",
      bullets: [
        "Utiliser le service de façon frauduleuse, y compris avec des moyens de paiement qui ne vous appartiennent pas.",
        "Tenter de contourner la sécurité, de perturber le service ou d’accéder à des comptes tiers.",
        "Endommager les bornes ou les utiliser en dehors de leur usage prévu.",
      ],
    },
    {
      heading: "7. Disponibilité et responsabilité",
      paragraphs: [
        "Nous faisons de notre mieux pour assurer un service continu, mais ne pouvons garantir l’absence d’interruption, notamment en cas de panne d’une borne, du réseau mobile ou d’un opérateur de paiement. Dans la limite permise par la loi, notre responsabilité est limitée aux dommages directs et prévisibles.",
      ],
    },
    {
      heading: "8. Suppression du compte",
      paragraphs: ["Vous pouvez supprimer votre compte à tout moment depuis l’application ou la page de suppression de ce site. Votre portefeuille doit être vide et aucune recharge ne doit être en cours. Retirez d’abord votre solde si nécessaire."],
    },
    {
      heading: "9. Modifications",
      paragraphs: ["Nous pouvons faire évoluer ces conditions. En cas de changement important, nous vous en informerons dans l’application. Continuer à utiliser le service vaut acceptation."],
    },
    {
      heading: "10. Droit applicable et contact",
      paragraphs: ["Ces conditions sont régies par le droit gabonais. Pour toute question, contactez-nous via la page Support."],
    },
  ],
};
