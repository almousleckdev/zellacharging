import type { LegalDocument } from "./legal-types";

export const deleteAccountEn: LegalDocument = {
  title: "Delete my account",
  intro: "You can delete your Zella Charging account and personal data at any time.",
  updatedLabel: "Last updated",
  sections: [
    {
      heading: "From the app",
      bullets: [
        "Open Zella Charging and sign in.",
        "Go to Profile, then Settings, then Delete my account.",
        "Confirm with your password, or with Google or Apple if you sign in that way, then confirm the deletion.",
      ],
    },
    {
      heading: "Before you delete",
      bullets: [
        "Your wallet must be empty: withdraw your balance to your mobile money number first.",
        "No charge may be in progress.",
      ],
    },
    {
      heading: "What is deleted",
      bullets: [
        "Name, email address, phone number, profile photo and sign-in identifiers.",
        "Your sign-in sessions and the devices registered for notifications.",
      ],
    },
    {
      heading: "What is kept",
      paragraphs: ["Financial records (charges, payments, withdrawals) are kept in de-identified form to meet accounting and legal obligations. They can no longer identify you."],
    },
    {
      heading: "Can’t use the app?",
      paragraphs: ["Write to us from your account email address through the Support page with the subject “Account deletion”. We will process your request after verifying your identity."],
    },
  ],
};
