import 'app_language.dart';

abstract final class WithdrawText {
  static String get title =>
      AppLanguage.pick(fr: 'Retirer de l’argent', en: 'Withdraw money');

  static String get explained => AppLanguage.pick(
    fr: 'Envoyez le solde de votre portefeuille vers votre compte Mobile Money. Le retrait est limité à votre solde disponible.',
    en: 'Send your wallet balance back to your mobile money account. Withdrawals are limited to your available balance.',
  );

  static String get amountTitle =>
      AppLanguage.pick(fr: 'Montant à retirer', en: 'Amount to withdraw');

  static String get all => AppLanguage.pick(fr: 'Tout', en: 'All');

  static String get destinationTitle =>
      AppLanguage.pick(fr: 'Destination', en: 'Destination');

  static String get destinationNote => AppLanguage.pick(
    fr: 'Seuls les numéros déjà utilisés pour recharger votre portefeuille sont acceptés.',
    en: 'Only numbers that already topped up your wallet are accepted.',
  );

  static String get numberLabel =>
      AppLanguage.pick(fr: 'Numéro Mobile Money', en: 'Mobile money number');

  static String get numberNotAllowed => AppLanguage.pick(
    fr: 'Ce numéro n’a jamais servi à recharger votre portefeuille.',
    en: 'This number has never topped up your wallet.',
  );

  static String get minimumHint =>
      AppLanguage.pick(fr: 'Minimum 200 FCFA', en: 'Minimum 200 FCFA');

  static String get pendingTitle =>
      AppLanguage.pick(fr: 'Demande envoyée', en: 'Request sent');

  static String pendingBody(String amount) => AppLanguage.pick(
    fr: 'Votre retrait de $amount est en attente de validation. Vous serez notifié dès qu’il sera traité.',
    en: 'Your withdrawal of $amount is awaiting approval. You will be notified as soon as it is handled.',
  );

  static String get noPayoutNumber => AppLanguage.pick(
    fr: 'Rechargez d’abord votre portefeuille avec Airtel Money ou Moov Money.',
    en: 'Top up your wallet with Airtel Money or Moov Money first.',
  );

  static String get passwordLabel =>
      AppLanguage.pick(fr: 'Mot de passe', en: 'Password');

  static String get passwordHint => AppLanguage.pick(
    fr: 'Confirmez avec votre mot de passe',
    en: 'Confirm with your password',
  );

  static String get toReceive =>
      AppLanguage.pick(fr: 'Vous recevez', en: 'You receive');

  static String get fromWallet =>
      AppLanguage.pick(fr: 'Débité du portefeuille', en: 'Taken from wallet');

  static String get invalidAmount => AppLanguage.pick(
    fr: 'Saisissez un montant d’au moins 200 et au plus votre solde disponible',
    en: 'Enter an amount of at least 200 and at most your available balance',
  );

  static String withdrawAmount(String amount) =>
      AppLanguage.pick(fr: 'Retirer $amount', en: 'Withdraw $amount');

  static String get progressTitle =>
      AppLanguage.pick(fr: 'Retrait en cours', en: 'Withdrawal in progress');

  static String progressBody(String operator, String phone) => AppLanguage.pick(
    fr: 'Nous envoyons l’argent vers votre compte $operator ($phone).',
    en: 'We are sending the money to your $operator account ($phone).',
  );

  static String get stepRequest =>
      AppLanguage.pick(fr: 'Demande enregistrée', en: 'Request recorded');

  static String get stepProcess => AppLanguage.pick(
    fr: 'Traitement par l’opérateur',
    en: 'Operator processing',
  );

  static String get stepDone =>
      AppLanguage.pick(fr: 'Argent envoyé', en: 'Money sent');

  static String get doneTitle =>
      AppLanguage.pick(fr: 'Retrait effectué', en: 'Withdrawal complete');

  static String doneBody(String amount) => AppLanguage.pick(
    fr: '$amount ont été envoyés sur votre compte Mobile Money.',
    en: '$amount were sent to your mobile money account.',
  );

  static String get failedTitle =>
      AppLanguage.pick(fr: 'Retrait non effectué', en: 'Withdrawal failed');

  static String get verifyingTitle => AppLanguage.pick(
    fr: 'Vérification en cours',
    en: 'Verification in progress',
  );

  static String get amountLabel =>
      AppLanguage.pick(fr: 'Montant à retirer', en: 'Amount to withdraw');

  static String get unavailable => AppLanguage.pick(
    fr: 'Les retraits seront bientôt disponibles.',
    en: 'Withdrawals will be available soon.',
  );

  static String get action => AppLanguage.pick(fr: 'Retirer', en: 'Withdraw');

  static String get hubTitle =>
      AppLanguage.pick(fr: 'Mon portefeuille', en: 'My wallet');

  static String get historyTitle =>
      AppLanguage.pick(fr: 'Historique', en: 'History');

  static String get emptyHistory => AppLanguage.pick(
    fr: 'Aucune opération pour le moment.',
    en: 'No transactions yet.',
  );

  static String entryLabel(String type) => switch (type) {
    'TOPUP' => AppLanguage.pick(
      fr: 'Crédit Mobile Money',
      en: 'Mobile money top-up',
    ),
    'CAPTURE' => AppLanguage.pick(fr: 'Recharge', en: 'Charging'),
    'WITHDRAWAL' => AppLanguage.pick(fr: 'Retrait', en: 'Withdrawal'),
    'WITHDRAWAL_REVERSAL' => AppLanguage.pick(
      fr: 'Retrait annulé',
      en: 'Withdrawal reversed',
    ),
    _ => type,
  };
}
