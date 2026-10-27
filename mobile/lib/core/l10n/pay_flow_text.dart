import 'app_language.dart';

abstract final class PayFlowText {
  static String get approveTitle => AppLanguage.pick(
    fr: 'Validez le paiement sur votre téléphone',
    en: 'Approve the payment on your phone',
  );

  static String approveBody(String operator, String phone) => AppLanguage.pick(
    fr: 'Une demande $operator a été envoyée au $phone. Saisissez votre code secret pour confirmer.',
    en: 'A $operator request was sent to $phone. Enter your secret code to confirm.',
  );

  static String get startingTitle =>
      AppLanguage.pick(fr: 'Démarrage de la borne', en: 'Starting the charger');

  static String get startingBody => AppLanguage.pick(
    fr: 'Paiement confirmé. Nous démarrons la recharge, ne quittez pas cet écran.',
    en: 'Payment confirmed. We are starting the charge, please stay on this screen.',
  );

  static String get stepRequest =>
      AppLanguage.pick(fr: 'Demande envoyée', en: 'Request sent');

  static String get stepApprove =>
      AppLanguage.pick(fr: 'Validation sur le téléphone', en: 'Phone approval');

  static String get stepConfirm =>
      AppLanguage.pick(fr: 'Paiement confirmé', en: 'Payment confirmed');

  static String get stepStart =>
      AppLanguage.pick(fr: 'Démarrage de la recharge', en: 'Charging starts');

  static String get topupDoneTitle =>
      AppLanguage.pick(fr: 'Portefeuille crédité', en: 'Wallet topped up');

  static String topupDoneBody(String amount) => AppLanguage.pick(
    fr: '$amount ont été ajoutés à votre portefeuille.',
    en: '$amount were added to your wallet.',
  );

  static String get failedTitle => AppLanguage.pick(
    fr: 'Paiement non confirmé',
    en: 'Payment not confirmed',
  );

  static String get verifyingTitle => AppLanguage.pick(
    fr: 'Vérification en cours',
    en: 'Verification in progress',
  );

  static String get retry => AppLanguage.pick(fr: 'Réessayer', en: 'Try again');

  static String get done => AppLanguage.pick(fr: 'Terminé', en: 'Done');

  static String get backToWallet =>
      AppLanguage.pick(fr: 'Retour au portefeuille', en: 'Back to wallet');
}
