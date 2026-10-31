import type { Locale } from "@/lib/site";

const contact = {
  fr: {
    title: "Contactez-nous",
    text: "Une question, un paiement bloqué, une demande sur vos données ? Écrivez-nous ou appelez-nous, nous répondons dès que possible.",
    phone: { label: "Téléphone", hint: "Appelez-nous" },
    general: { label: "Contact", hint: "Questions générales et assistance" },
    team: { label: "Équipe", hint: "Écrire à l’équipe Zella" },
    privacy: { label: "Confidentialité", hint: "Données personnelles et suppression de compte" },
    include: "Indiquez l’adresse e-mail de votre compte et, si possible, la référence du paiement ou de la recharge concernée.",
    form: {
      title: "Envoyez-nous un message",
      name: "Nom complet",
      email: "Adresse e-mail",
      phone: "Téléphone (facultatif)",
      topic: "Sujet",
      topics: ["Question générale", "Paiement ou retrait", "Recharge ou borne", "Compte et données personnelles", "Partenariat"],
      message: "Votre message",
      send: "Envoyer le message",
      sending: "Envoi en cours…",
      sent: "Merci, votre message a bien été envoyé. Nous vous répondons dès que possible.",
      invalid: "Vérifiez votre nom, votre adresse e-mail et votre message (10 caractères minimum).",
      failed: "Votre message n’a pas pu être envoyé. Réessayez ou écrivez-nous directement par e-mail.",
    },
  },
  en: {
    title: "Contact us",
    text: "A question, a stuck payment, a request about your data? Write to us or call us and we will reply as soon as possible.",
    phone: { label: "Phone", hint: "Call us" },
    general: { label: "Contact", hint: "General questions and help" },
    team: { label: "Team", hint: "Write to the Zella team" },
    privacy: { label: "Privacy", hint: "Personal data and account deletion" },
    include: "Include your account email address and, if possible, the reference of the payment or charge concerned.",
    form: {
      title: "Send us a message",
      name: "Full name",
      email: "Email address",
      phone: "Phone (optional)",
      topic: "Topic",
      topics: ["General question", "Payment or withdrawal", "Charge or charger", "Account and personal data", "Partnership"],
      message: "Your message",
      send: "Send message",
      sending: "Sending…",
      sent: "Thank you, your message was sent. We will reply as soon as possible.",
      invalid: "Check your name, email address and message (at least 10 characters).",
      failed: "Your message could not be sent. Try again or write to us directly by email.",
    },
  },
};

export const contactFor = (locale: Locale) => contact[locale];
export type ContactCopy = ReturnType<typeof contactFor>;
