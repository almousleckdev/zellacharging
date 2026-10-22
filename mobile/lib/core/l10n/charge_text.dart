import 'app_language.dart';

abstract final class ChargeText {
  static String get scanToCharge =>
      AppLanguage.pick(fr: 'Scanner pour recharger', en: 'Scan to charge');

  static String get setupTitle =>
      AppLanguage.pick(fr: 'Démarrer la recharge', en: 'Start charging');

  static String get setupAmountQuestion => AppLanguage.pick(
    fr: 'Quel montant souhaitez-vous recharger ?',
    en: 'How much do you want to charge?',
  );

  static String get amountLabel =>
      AppLanguage.pick(fr: 'Montant', en: 'Amount');

  static String get amountHint =>
      AppLanguage.pick(fr: 'ex. 2000', en: 'e.g. 2000');

  static String get invalidAmount => AppLanguage.pick(
    fr: 'Saisissez un montant d\'au moins 1000',
    en: 'Enter an amount of at least 1000',
  );

  static String get payment => AppLanguage.pick(fr: 'Paiement', en: 'Payment');

  static String get paidFromWallet => AppLanguage.pick(
    fr: 'Payé avec le solde de votre portefeuille',
    en: 'Paid from your wallet balance',
  );

  static String get toPayByMobileMoney => AppLanguage.pick(
    fr: 'À payer par Mobile Money',
    en: 'To pay by mobile money',
  );

  static String get unusedReturned => AppLanguage.pick(
    fr: 'Tout montant non utilisé est reversé dans votre portefeuille.',
    en: 'Any unused amount goes back to your wallet.',
  );

  static String get startAndPay =>
      AppLanguage.pick(fr: 'Payer et démarrer', en: 'Pay and start');

  static String get startOnly =>
      AppLanguage.pick(fr: 'Démarrer la recharge', en: 'Start charging');

  static String get chargingTitle =>
      AppLanguage.pick(fr: 'Recharge', en: 'Charging');

  static String get stopCharging =>
      AppLanguage.pick(fr: 'Arrêter la recharge', en: 'Stop charging');

  static String get stopConfirmTitle =>
      AppLanguage.pick(fr: 'Arrêter la recharge ?', en: 'Stop charging?');

  static String get stopConfirmBody => AppLanguage.pick(
    fr: 'La session prendra fin et le montant final sera prélevé automatiquement sur votre portefeuille.',
    en: 'The session will end and the final amount is paid automatically from your wallet.',
  );

  static String get unplugNotice => AppLanguage.pick(
    fr: 'Ne débranchez pas le câble pendant la recharge. Arrêtez d\'abord la recharge ici, puis débranchez.',
    en: 'Do not unplug the cable while charging. Stop the charge here first, then unplug.',
  );

  static String get energy => AppLanguage.pick(fr: 'Énergie', en: 'Energy');

  static String get power => AppLanguage.pick(fr: 'Puissance', en: 'Power');

  static String get battery => AppLanguage.pick(fr: 'Batterie', en: 'Battery');

  static String get duration => AppLanguage.pick(fr: 'Durée', en: 'Duration');

  static String get cost => AppLanguage.pick(fr: 'Coût', en: 'Cost');

  static String get price => AppLanguage.pick(fr: 'Prix', en: 'Price');

  static String get amountSoFar =>
      AppLanguage.pick(fr: 'Montant actuel', en: 'Amount so far');

  static String get amountPaid =>
      AppLanguage.pick(fr: 'Montant payé', en: 'Amount paid');

  static String get estimated =>
      AppLanguage.pick(fr: 'Estimé', en: 'Estimated');

  static String get charged => AppLanguage.pick(fr: 'Rechargé', en: 'Charged');

  static String get voltage => AppLanguage.pick(fr: 'Tension', en: 'Voltage');

  static String get current => AppLanguage.pick(fr: 'Courant', en: 'Current');

  static String get activeSession =>
      AppLanguage.pick(fr: 'Recharge en cours', en: 'Charge in progress');

  static String get viewSession => AppLanguage.pick(fr: 'Voir', en: 'View');

  static String get waitingForPlug => AppLanguage.pick(
    fr: 'En attente du démarrage de la borne...',
    en: 'Waiting for the charger to start...',
  );

  static String get finishing => AppLanguage.pick(
    fr: 'Arrêt de la recharge...',
    en: 'Stopping the charge...',
  );

  static String get sessionFailed => AppLanguage.pick(
    fr: 'La recharge n\'a pas pu démarrer.',
    en: 'The charge could not be started.',
  );

  static const chargeAmounts = [1000, 2000, 5000, 10000];
  static const minimumChargeMinor = 1000;

  static String updatedAgo(String age) =>
      AppLanguage.pick(fr: 'Mis à jour il y a $age', en: 'Updated $age ago');

  static String lastReadingAgo(String age) => AppLanguage.pick(
    fr: 'Dernière mesure il y a $age',
    en: 'Last reading $age ago',
  );

  static Map<String, String> get sessionStates => {
    'requested': AppLanguage.pick(fr: 'Démarrage', en: 'Starting'),
    'startAccepted': AppLanguage.pick(
      fr: 'En attente du véhicule',
      en: 'Waiting for vehicle',
    ),
    'active': chargingStatus,
    'stopping': AppLanguage.pick(fr: 'Arrêt', en: 'Stopping'),
    'completed': AppLanguage.pick(fr: 'Terminée', en: 'Completed'),
    'failed': AppLanguage.pick(fr: 'Échec', en: 'Failed'),
    'abandoned': AppLanguage.pick(fr: 'Anormale', en: 'Abnormal'),
  };

  static String get chargingStatus =>
      AppLanguage.pick(fr: 'En charge', en: 'Charging');
}
