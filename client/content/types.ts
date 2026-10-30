export type Feature = { title: string; text: string };
export type Step = { title: string; text: string };
export type Faq = { question: string; answer: string };

export type Copy = {
  meta: { title: string; description: string };
  nav: { features: string; how: string; faq: string; contact: string };
  hero: { title: string; highlight: string; text: string };
  stores: { appStore: string; playStore: string; appStoreLead: string; playStoreLead: string; soon: string };
  features: { title: string; text: string; items: Feature[] };
  how: { title: string; steps: Step[] };
  payments: { title: string; text: string };
  faq: { title: string; items: Faq[] };
  cta: { title: string; text: string };
  footer: { product: string; legal: string; privacy: string; terms: string; deleteAccount: string; contact: string; rights: string };
  switchTo: { label: string; locale: "fr" | "en" };
};
