import type { LegalDocument } from "./legal-types";

export const privacyFr: LegalDocument = {
  title: "Politique de confidentialité",
  intro:
    "Cette politique explique quelles données Zella Charging collecte, pourquoi, avec qui elles sont partagées et comment vous gardez le contrôle. Nous collectons le minimum nécessaire pour vous permettre de recharger votre véhicule et de payer.",
  updatedLabel: "Dernière mise à jour",
  sections: [
    {
      heading: "1. Qui est responsable de vos données",
      paragraphs: ["Le responsable du traitement est l’éditeur de l’application Zella Charging. Pour toute question, écrivez-nous à confidentialite@zella-tech.com."],
    },
    {
      heading: "2. Les données que nous collectons",
      bullets: [
        "Compte : prénom, nom, adresse e-mail, numéro de téléphone (facultatif) et mot de passe. Le mot de passe est conservé sous forme chiffrée irréversible et ne peut pas être lu.",
        "Connexion avec Google ou Apple : identifiant du fournisseur, adresse e-mail vérifiée et nom si le fournisseur le transmet. Nous ne recevons jamais votre mot de passe Google ou Apple.",
        "Photo de profil (facultative) : choisie dans votre galerie ou prise avec l’appareil photo.",
        "Recharges : borne, connecteur, heures de début et de fin, énergie délivrée et coût.",
        "Paiements : numéro Mobile Money, opérateur, montants, statut et références de transaction. Nous ne collectons aucune donnée de carte bancaire.",
        "Appareil : jeton de notification, système (iOS ou Android) et langue, afin de vous envoyer vos notifications.",
        "Position : utilisée pour afficher les bornes proches de vous. Nous ne conservons pas d’historique de vos déplacements.",
        "Appareil photo : utilisé pour scanner le QR code d’une borne et pour la photo de profil.",
      ],
    },
    {
      heading: "3. Pourquoi nous utilisons ces données",
      bullets: [
        "Créer et sécuriser votre compte, et vous authentifier.",
        "Démarrer, suivre et facturer vos recharges.",
        "Traiter vos crédits de portefeuille et vos retraits, et prévenir la fraude.",
        "Vous envoyer des notifications liées à vos recharges et à vos paiements.",
        "Répondre à vos demandes de support et respecter nos obligations légales et comptables.",
      ],
    },
    {
      heading: "4. Avec qui nous partageons des données",
      paragraphs: ["Nous ne vendons pas vos données. Nous les partageons uniquement avec les prestataires nécessaires au service :"],
      bullets: [
        "Les opérateurs Mobile Money (Airtel Money, Moov Money) et notre prestataire de paiement, pour traiter les crédits et les retraits.",
        "L’exploitant du réseau de bornes, pour démarrer et arrêter les recharges.",
        "Google (Firebase Cloud Messaging) et Apple (service de notifications push), pour acheminer les notifications.",
        "Google et Apple, si vous choisissez de vous connecter avec leur service.",
        "Notre hébergeur d’infrastructure et notre service d’envoi d’e-mails.",
        "OpenStreetMap, dont les serveurs fournissent les images de la carte : votre adresse IP est visible de ce service.",
      ],
    },
    {
      heading: "5. Durée de conservation",
      paragraphs: [
        "Nous conservons vos données tant que votre compte est actif. À la suppression du compte, vos informations personnelles (nom, e-mail, téléphone, photo, identifiants de connexion) sont effacées. Les écritures financières (recharges, paiements, retraits) sont conservées sous forme dépersonnalisée pendant la durée exigée par les obligations comptables et légales.",
      ],
    },
    {
      heading: "6. Vos droits",
      bullets: [
        "Accéder à vos données et les corriger depuis l’écran Profil.",
        "Supprimer votre compte depuis l’application (Profil, Paramètres, Supprimer mon compte) ou via la page de suppression de ce site.",
        "Retirer votre consentement aux notifications ou à la localisation dans les réglages de votre téléphone.",
        "Nous contacter pour toute demande d’accès, de rectification ou d’opposition. Vous pouvez aussi saisir l’autorité de protection des données de votre pays.",
      ],
    },
    {
      heading: "7. Sécurité",
      paragraphs: ["Les échanges sont chiffrés (HTTPS). Les mots de passe sont chiffrés de façon irréversible, les sessions expirent et les accès sont limités au personnel autorisé. Aucun système n’étant infaillible, signalez-nous tout incident à confidentialite@zella-tech.com."],
    },
    {
      heading: "8. Transferts internationaux",
      paragraphs: ["Certains prestataires (hébergement, notifications, connexion Google ou Apple) peuvent traiter des données hors du Gabon. Nous choisissons des prestataires qui appliquent des garanties de protection appropriées."],
    },
    {
      heading: "9. Enfants",
      paragraphs: ["Zella Charging s’adresse aux personnes de 18 ans et plus. Nous ne collectons pas sciemment de données concernant des mineurs."],
    },
    {
      heading: "10. Modifications",
      paragraphs: ["Nous pouvons mettre à jour cette politique. La date de dernière mise à jour figure en haut de la page ; en cas de changement important, nous vous en informerons dans l’application."],
    },
    { heading: "11. Contact", paragraphs: ["Pour toute question sur vos données, écrivez-nous à confidentialite@zella-tech.com ou utilisez la page Contact."] },
  ],
};
