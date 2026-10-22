import 'app_language.dart';

abstract final class HistoryText {
  static String get recentSessions =>
      AppLanguage.pick(fr: 'Recharges récentes', en: 'Recent charges');

  static String get chargingHistory =>
      AppLanguage.pick(fr: 'Historique des recharges', en: 'Charging history');

  static String get totalSessions =>
      AppLanguage.pick(fr: 'Recharges', en: 'Charges');

  static String get totalEnergy =>
      AppLanguage.pick(fr: 'Énergie', en: 'Energy');

  static String get totalSpent => AppLanguage.pick(fr: 'Dépensé', en: 'Spent');

  static String get historyEmpty => AppLanguage.pick(
    fr: 'Vos recharges apparaîtront ici.',
    en: 'Your charges will appear here.',
  );

  static String get detailsTitle =>
      AppLanguage.pick(fr: 'Détails', en: 'Details');

  static String get totalDuration =>
      AppLanguage.pick(fr: 'Durée totale', en: 'Total duration');

  static String get totalKwh =>
      AppLanguage.pick(fr: 'Total kWh', en: 'Total kWh');

  static String get totalFee =>
      AppLanguage.pick(fr: 'Frais totaux', en: 'Total fee');

  static String get locationTitle =>
      AppLanguage.pick(fr: 'Lieu', en: 'Location');

  static String get feeDetails =>
      AppLanguage.pick(fr: 'Détail des frais', en: 'Fee details');

  static String get electricFee =>
      AppLanguage.pick(fr: 'Électricité', en: 'Electric fee');

  static String get vat => AppLanguage.pick(fr: 'TVA', en: 'VAT');

  static String get serviceFee =>
      AppLanguage.pick(fr: 'Frais de service', en: 'Service fee');

  static String get total => AppLanguage.pick(fr: 'Total', en: 'Total');

  static String get deviceInfo =>
      AppLanguage.pick(fr: 'Informations de la borne', en: 'Device info');

  static String get deviceNo =>
      AppLanguage.pick(fr: 'N° de la borne', en: 'Device No');

  static String get dcPower => AppLanguage.pick(fr: 'DC', en: 'DC');

  static String get voltageLabel =>
      AppLanguage.pick(fr: 'Tension', en: 'Voltage');

  static String get startTime =>
      AppLanguage.pick(fr: 'Heure de début', en: 'Start time');

  static String get endTime =>
      AppLanguage.pick(fr: 'Heure de fin', en: 'End time');

  static String get deleteCharge =>
      AppLanguage.pick(fr: 'Supprimer', en: 'Delete');

  static String get deleteTitle => AppLanguage.pick(
    fr: 'Supprimer cette recharge ?',
    en: 'Delete this charge?',
  );

  static String get deleteBody => AppLanguage.pick(
    fr: 'Elle disparaîtra de votre activité. Les montants déjà payés ne changent pas.',
    en: 'It will disappear from your activity. Amounts already paid do not change.',
  );

  static String get chargeDeleted =>
      AppLanguage.pick(fr: 'Recharge supprimée', en: 'Charge deleted');

  static const gps = 'GPS';

  static List<String> get filters => [
    AppLanguage.pick(fr: 'Tous', en: 'All'),
    AppLanguage.pick(fr: 'En cours', en: 'Charging'),
    AppLanguage.pick(fr: 'Terminées', en: 'Completed'),
    AppLanguage.pick(fr: 'Anormales', en: 'Abnormal'),
  ];
}
