import type { LegalDocument } from "./legal-types";

export const termsEn: LegalDocument = {
  title: "Terms of use",
  intro:
    "By creating an account or using Zella Charging, you accept the terms below. Please read them carefully.",
  updatedLabel: "Last updated",
  sections: [
    {
      heading: "1. The service",
      paragraphs: ["Zella Charging is an app to find electric vehicle chargers, start and follow a charge and pay with a wallet funded by mobile money. The chargers are operated by a partner network."],
    },
    {
      heading: "2. Your account",
      bullets: [
        "You must be 18 or older and provide accurate information.",
        "You are responsible for keeping your password confidential and for the activity on your account.",
        "We may suspend an account in case of fraud, abuse or breach of these terms.",
      ],
    },
    {
      heading: "3. Wallet and payments",
      bullets: [
        "You top up your wallet with Airtel Money or Moov Money. The amount is available once the operator confirms the payment.",
        "Each charge is debited from the wallet. The price and any fees are shown before you start.",
        "The wallet balance is not a bank account and earns no interest.",
      ],
    },
    {
      heading: "4. Withdrawals",
      bullets: [
        "You can withdraw your available balance only to a mobile money number already used to top up your wallet.",
        "The minimum withdrawal is 500 FCFA.",
        "Each withdrawal is reviewed and approved by our team before it is sent. If it is declined, the amount returns to your wallet.",
      ],
    },
    {
      heading: "5. Charging safely",
      bullets: [
        "Check that the connector is compatible with your vehicle before you start.",
        "You remain responsible for your vehicle and the charging area during the whole charge.",
        "You can stop a charge at any time from the app or at the charger.",
      ],
    },
    {
      heading: "6. Prohibited use",
      bullets: [
        "Using the service fraudulently, including with payment methods that are not yours.",
        "Trying to bypass security, disrupt the service or access other people’s accounts.",
        "Damaging chargers or using them outside their intended purpose.",
      ],
    },
    {
      heading: "7. Availability and liability",
      paragraphs: [
        "We work to keep the service running but cannot guarantee it will never be interrupted, in particular if a charger, the mobile network or a payment operator fails. To the extent permitted by law, our liability is limited to direct and foreseeable damages.",
      ],
    },
    {
      heading: "8. Deleting your account",
      paragraphs: ["You can delete your account at any time from the app or the deletion page of this website. Your wallet must be empty and no charge may be in progress. Withdraw your balance first if needed."],
    },
    {
      heading: "9. Changes",
      paragraphs: ["We may update these terms. For important changes we will tell you in the app. Continuing to use the service means you accept them."],
    },
    {
      heading: "10. Governing law and contact",
      paragraphs: ["These terms are governed by the law of Gabon. For any question, contact us through the Contact page."],
    },
  ],
};
