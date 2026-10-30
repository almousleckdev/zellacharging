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
  },
  en: {
    title: "Contact us",
    text: "A question, a stuck payment, a request about your data? Write to us or call us and we will reply as soon as possible.",
    phone: { label: "Phone", hint: "Call us" },
    general: { label: "Contact", hint: "General questions and help" },
    team: { label: "Team", hint: "Write to the Zella team" },
    privacy: { label: "Privacy", hint: "Personal data and account deletion" },
    include: "Include your account email address and, if possible, the reference of the payment or charge concerned.",
  },
};

export const contactFor = (locale: Locale) => contact[locale];
