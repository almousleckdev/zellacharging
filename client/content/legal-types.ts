export type LegalSection = { heading: string; paragraphs?: string[]; bullets?: string[] };
export type LegalDocument = { title: string; intro: string; updatedLabel: string; sections: LegalSection[] };
