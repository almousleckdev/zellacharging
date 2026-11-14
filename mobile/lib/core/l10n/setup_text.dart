import 'app_language.dart';

abstract final class SetupText {
  static String get amountTitle =>
      AppLanguage.pick(fr: 'Montant à recharger', en: 'Amount to charge');

  static String get amountHint => AppLanguage.pick(
    fr: 'Entrez le montant souhaité',
    en: 'Enter the amount you want',
  );

  static String get plugInFirst => AppLanguage.pick(
    fr: 'Branchez le câble sur votre véhicule pour démarrer',
    en: 'Plug the cable into your vehicle to start',
  );

  static String get enterAmountFirst => AppLanguage.pick(
    fr: 'Entrez un montant pour continuer',
    en: 'Enter an amount to continue',
  );

  static String tooLow(String minimum) => AppLanguage.pick(
    fr: 'Le minimum est $minimum',
    en: 'The minimum is $minimum',
  );

  static String tooHigh(String maximum) => AppLanguage.pick(
    fr: 'Le maximum est $maximum',
    en: 'The maximum is $maximum',
  );

  static String exceedsBalance(String balance) => AppLanguage.pick(
    fr: 'Solde insuffisant : $balance disponibles',
    en: 'Insufficient balance: $balance available',
  );

  static String startFor(String amount) =>
      AppLanguage.pick(fr: 'Démarrer · $amount', en: 'Start · $amount');

  static String get cablePlugged => AppLanguage.pick(
    fr: 'Câble branché · prêt à charger',
    en: 'Cable plugged in · ready to charge',
  );

  static String get cableWaiting =>
      AppLanguage.pick(fr: 'En attente du câble', en: 'Waiting for the cable');

  static String get summaryTitle =>
      AppLanguage.pick(fr: 'Récapitulatif', en: 'Summary');

  static String get summaryAmount =>
      AppLanguage.pick(fr: 'Montant réservé', en: 'Amount reserved');

  static String get summaryEnergy =>
      AppLanguage.pick(fr: 'Énergie estimée', en: 'Estimated energy');

  static String get freeNote => AppLanguage.pick(
    fr: 'Recharge gratuite : la charge continue jusqu’à l’arrêt.',
    en: 'Free charging: the charge runs until you stop it.',
  );
}
