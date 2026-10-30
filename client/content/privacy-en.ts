import type { LegalDocument } from "./legal-types";

export const privacyEn: LegalDocument = {
  title: "Privacy policy",
  intro:
    "This policy explains what data Zella Charging collects, why, who it is shared with and how you stay in control. We collect the minimum needed to let you charge your vehicle and pay.",
  updatedLabel: "Last updated",
  sections: [
    {
      heading: "1. Who is responsible for your data",
      paragraphs: ["The data controller is the publisher of the Zella Charging app. For any question, write to us at confidentialite@zella-tech.com."],
    },
    {
      heading: "2. Data we collect",
      bullets: [
        "Account: first name, last name, email address, phone number (optional) and password. The password is stored in an irreversible encrypted form and cannot be read.",
        "Sign in with Google or Apple: provider identifier, verified email address and name if the provider shares it. We never receive your Google or Apple password.",
        "Profile photo (optional): chosen from your gallery or taken with the camera.",
        "Charging: charger, connector, start and end times, energy delivered and cost.",
        "Payments: mobile money number, operator, amounts, status and transaction references. We collect no bank card data.",
        "Device: notification token, system (iOS or Android) and language, so we can send you notifications.",
        "Location: used to show chargers near you. We do not keep a history of your movements.",
        "Camera: used to scan a charger QR code and for the profile photo.",
      ],
    },
    {
      heading: "3. Why we use this data",
      bullets: [
        "To create and secure your account and to authenticate you.",
        "To start, follow and bill your charges.",
        "To process wallet top-ups and withdrawals, and to prevent fraud.",
        "To send notifications about your charges and payments.",
        "To answer support requests and meet our legal and accounting obligations.",
      ],
    },
    {
      heading: "4. Who we share data with",
      paragraphs: ["We do not sell your data. We share it only with the providers needed to run the service:"],
      bullets: [
        "Mobile money operators (Airtel Money, Moov Money) and our payment provider, to process top-ups and withdrawals.",
        "The charging network operator, to start and stop charges.",
        "Google (Firebase Cloud Messaging) and Apple (push notification service), to deliver notifications.",
        "Google and Apple, if you choose to sign in with their service.",
        "Our infrastructure host and our email delivery service.",
        "OpenStreetMap, whose servers provide the map images: your IP address is visible to that service.",
      ],
    },
    {
      heading: "5. Retention",
      paragraphs: [
        "We keep your data while your account is active. When you delete your account, your personal information (name, email, phone, photo, sign-in identifiers) is erased. Financial records (charges, payments, withdrawals) are kept in de-identified form for as long as accounting and legal obligations require.",
      ],
    },
    {
      heading: "6. Your rights",
      bullets: [
        "Access and correct your data from the Profile screen.",
        "Delete your account from the app (Profile, Settings, Delete my account) or from the deletion page of this website.",
        "Withdraw your consent to notifications or location in your phone settings.",
        "Contact us for any access, rectification or objection request. You may also contact the data protection authority of your country.",
      ],
    },
    {
      heading: "7. Security",
      paragraphs: ["Traffic is encrypted (HTTPS). Passwords are irreversibly encrypted, sessions expire and access is limited to authorised staff. No system is perfect, so please report any incident to confidentialite@zella-tech.com."],
    },
    {
      heading: "8. International transfers",
      paragraphs: ["Some providers (hosting, notifications, Google or Apple sign-in) may process data outside Gabon. We choose providers that apply appropriate safeguards."],
    },
    {
      heading: "9. Children",
      paragraphs: ["Zella Charging is intended for people aged 18 and over. We do not knowingly collect data about minors."],
    },
    {
      heading: "10. Changes",
      paragraphs: ["We may update this policy. The last update date is shown at the top of the page; for important changes we will tell you in the app."],
    },
    { heading: "11. Contact", paragraphs: ["For any question about your data, write to us at confidentialite@zella-tech.com or use the Contact page."] },
  ],
};
