import 'app_language.dart';

abstract final class WalletHistoryText {
  static String get detailsTitle =>
      AppLanguage.pick(fr: 'Détail de l’opération', en: 'Transaction details');

  static String get today => AppLanguage.pick(fr: 'Aujourd’hui', en: 'Today');

  static String get yesterday => AppLanguage.pick(fr: 'Hier', en: 'Yesterday');

  static String get operationTitle =>
      AppLanguage.pick(fr: 'Opération', en: 'Transaction');

  static String get type => AppLanguage.pick(fr: 'Type', en: 'Type');

  static String get date => AppLanguage.pick(fr: 'Date', en: 'Date');

  static String get time => AppLanguage.pick(fr: 'Heure', en: 'Time');

  static String get status => AppLanguage.pick(fr: 'Statut', en: 'Status');

  static String get reference =>
      AppLanguage.pick(fr: 'Référence Zella', en: 'Zella reference');

  static String get mobileMoneyTitle =>
      AppLanguage.pick(fr: 'Compte Mobile Money', en: 'Mobile money account');

  static String get operator =>
      AppLanguage.pick(fr: 'Opérateur', en: 'Operator');

  static String get number => AppLanguage.pick(fr: 'Numéro', en: 'Number');

  static String get providerReference =>
      AppLanguage.pick(fr: 'Référence opérateur', en: 'Operator reference');

  static String get walletTitle =>
      AppLanguage.pick(fr: 'Portefeuille', en: 'Wallet');

  static String get amount => AppLanguage.pick(fr: 'Montant', en: 'Amount');

  static String get balanceAfter => AppLanguage.pick(
    fr: 'Solde après l’opération',
    en: 'Balance after the transaction',
  );

  static String get viewCharge =>
      AppLanguage.pick(fr: 'Voir la recharge', en: 'View the charge');

  static String get deleteTitle => AppLanguage.pick(
    fr: 'Supprimer cette opération ?',
    en: 'Delete this transaction?',
  );

  static String get deleteBody => AppLanguage.pick(
    fr: 'Elle disparaîtra de votre historique. Votre solde ne change pas et l’opération reste conservée pour la sécurité de votre compte.',
    en: 'It will disappear from your history. Your balance does not change and the record is kept for your account security.',
  );

  static String get delete => AppLanguage.pick(fr: 'Supprimer', en: 'Delete');

  static String get deleted =>
      AppLanguage.pick(fr: 'Opération supprimée', en: 'Transaction deleted');

  static String statusLabel(String? state) => switch (state) {
    'PAID' => AppLanguage.pick(fr: 'Envoyé', en: 'Sent'),
    'SUCCEEDED' => AppLanguage.pick(fr: 'Confirmé', en: 'Confirmed'),
    'PENDING_APPROVAL' => AppLanguage.pick(
      fr: 'En attente de validation',
      en: 'Awaiting approval',
    ),
    'PENDING' ||
    'REQUESTED' ||
    'PROCESSING' => AppLanguage.pick(fr: 'En cours', en: 'In progress'),
    'REVIEW' => AppLanguage.pick(fr: 'En vérification', en: 'Being verified'),
    'FAILED' ||
    'EXPIRED' => AppLanguage.pick(fr: 'Non abouti', en: 'Not completed'),
    _ => AppLanguage.pick(fr: 'Terminé', en: 'Completed'),
  };
}
