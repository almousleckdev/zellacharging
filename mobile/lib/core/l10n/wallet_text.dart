import 'app_language.dart';

abstract final class WalletText {
  static String get topUp => AppLanguage.pick(fr: 'Créditer', en: 'Top up');

  static String get topUpTitle =>
      AppLanguage.pick(fr: 'Créditer le portefeuille', en: 'Top up wallet');

  static String get topUpBody => AppLanguage.pick(
    fr: 'Payez par Mobile Money. Le montant est ajouté instantanément à votre portefeuille.',
    en: 'Pay with mobile money. The amount is added to your wallet instantly.',
  );

  static String get topupAmountLabel =>
      AppLanguage.pick(fr: 'Montant à créditer', en: 'Amount to add');

  static String get topupAmountHint =>
      AppLanguage.pick(fr: 'Ex. 5 000', en: 'e.g. 5,000');

  static String get chooseAmount =>
      AppLanguage.pick(fr: 'Montant', en: 'Amount');

  static String get chooseOperator =>
      AppLanguage.pick(fr: 'Mobile Money', en: 'Mobile money');

  static String get mobileMoneyNumberLabel =>
      AppLanguage.pick(fr: 'Numéro Mobile Money', en: 'Mobile money number');

  static String get mobileMoneyNumberHint =>
      AppLanguage.pick(fr: '077 12 34 56', en: '077 12 34 56');

  static String get invalidMobileMoney => AppLanguage.pick(
    fr: 'Saisissez un numéro valide, ex. 077 12 34 56',
    en: 'Enter a valid number, e.g. 077 12 34 56',
  );

  static String get payNow => AppLanguage.pick(fr: 'Payer', en: 'Pay');

  static String get topUpDone =>
      AppLanguage.pick(fr: 'Crédit réussi', en: 'Top-up successful');

  static String get topUpPending => AppLanguage.pick(
    fr: 'En attente de votre validation sur le téléphone...',
    en: 'Waiting for your approval on the phone...',
  );

  static String get topUpFailed => AppLanguage.pick(
    fr: 'Le paiement n\'a pas abouti.',
    en: 'The payment was not completed.',
  );

  static const minimumTopupMinor = 500;

  static const minimumWithdrawMinor = 1000;

  static String get invalidTopupAmount => AppLanguage.pick(
    fr: 'Saisissez un montant d\'au moins 500',
    en: 'Enter an amount of at least 500',
  );
}
