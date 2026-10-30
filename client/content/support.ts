import type { Locale } from "@/lib/site";

const support = {
  fr: {
    title: "Support",
    text: "Une question, un paiement bloqué, une demande sur vos données ? Écrivez-nous, nous répondons dès que possible.",
    email: "E-mail",
    phone: "Téléphone",
    include: "Indiquez l’adresse e-mail de votre compte et, si possible, la référence du paiement ou de la recharge concernée.",
  },
  en: {
    title: "Support",
    text: "A question, a stuck payment, a request about your data? Write to us and we will reply as soon as possible.",
    email: "Email",
    phone: "Phone",
    include: "Include your account email address and, if possible, the reference of the payment or charge concerned.",
  },
};

export const supportFor = (locale: Locale) => support[locale];
