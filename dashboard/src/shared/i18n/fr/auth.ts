export const frAuth = {
  login: {
    title: "Connexion à la console",
    subtitle: "Accédez à l'administration de la plateforme Zella Charging.",
    email: "Adresse e-mail",
    emailPlaceholder: "nom@entreprise.com",
    password: "Mot de passe",
    showPassword: "Afficher le mot de passe",
    hidePassword: "Masquer le mot de passe",
    submit: "Se connecter",
    submitting: "Connexion en cours…",
  },
  totp: {
    requiredTitle: "Vérification en deux étapes",
    requiredSubtitle: "Saisissez le code à 6 chiffres affiché par votre application d'authentification.",
    enrollTitle: "Sécurisez votre compte",
    enrollSubtitle:
      "L'authentification à deux facteurs est obligatoire pour accéder à la console. Configurez-la une seule fois.",
    stepScan: "Scannez ce QR code avec votre application d'authentification.",
    stepEnter: "Saisissez ensuite le code à 6 chiffres généré.",
    qrLabel: "QR code de configuration de l'authentification à deux facteurs",
    manualKey: "Ou saisissez cette clé manuellement",
    copy: "Copier la clé",
    copied: "Clé copiée",
    code: "Code de vérification",
    verify: "Vérifier et continuer",
    verifying: "Vérification…",
    back: "Retour à la connexion",
  },
  panel: {
    title: "Pilotez votre réseau de recharge en toute confiance",
    items: [
      {
        title: "Supervision en temps réel",
        text: "Bornes, sessions de charge et incidents réunis sur un seul écran.",
      },
      {
        title: "Gabon et Mali",
        text: "Une console unique, des données strictement cloisonnées par pays.",
      },
      {
        title: "Finance maîtrisée",
        text: "Portefeuilles, paiements, retraits et réconciliation sous contrôle.",
      },
    ],
    security: "Accès réservé aux administrateurs autorisés. Chaque action est journalisée.",
  },
} as const;
