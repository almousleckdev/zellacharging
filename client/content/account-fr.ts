import type { LegalDocument } from "./legal-types";

export const deleteAccountFr: LegalDocument = {
  title: "Supprimer mon compte",
  intro: "Vous pouvez supprimer votre compte Zella Charging et vos données personnelles à tout moment.",
  updatedLabel: "Dernière mise à jour",
  sections: [
    {
      heading: "Depuis l’application",
      bullets: [
        "Ouvrez Zella Charging et connectez-vous.",
        "Allez dans Profil, puis Paramètres, puis Supprimer mon compte.",
        "Confirmez avec votre mot de passe, ou avec Google ou Apple si vous vous connectez ainsi, puis validez la suppression.",
      ],
    },
    {
      heading: "Avant de supprimer",
      bullets: [
        "Votre portefeuille doit être vide : retirez d’abord votre solde vers votre numéro Mobile Money.",
        "Aucune recharge ne doit être en cours.",
      ],
    },
    {
      heading: "Ce qui est supprimé",
      bullets: [
        "Nom, adresse e-mail, numéro de téléphone, photo de profil et identifiants de connexion.",
        "Vos sessions de connexion et vos appareils enregistrés pour les notifications.",
      ],
    },
    {
      heading: "Ce qui est conservé",
      paragraphs: ["Les écritures financières (recharges, paiements, retraits) sont conservées sous forme dépersonnalisée pour répondre aux obligations comptables et légales. Elles ne permettent plus de vous identifier."],
    },
    {
      heading: "Vous ne pouvez pas utiliser l’application ?",
      paragraphs: ["Écrivez-nous depuis l’adresse e-mail de votre compte via la page Support en indiquant « Suppression de compte ». Nous traiterons votre demande après vérification de votre identité."],
    },
  ],
};
