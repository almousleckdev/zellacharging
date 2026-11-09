import 'app_language.dart';

abstract final class HistoryText {
  static String get batteryLevels =>
      AppLanguage.pick(fr: 'Niveau de batterie', en: 'Battery level');

  static String get batteryStart =>
      AppLanguage.pick(fr: 'Au départ', en: 'At start');

  static String get batteryEnd =>
      AppLanguage.pick(fr: 'À l’arrivée', en: 'At end');

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

  static String get totalAmount =>
      AppLanguage.pick(fr: 'Montant total', en: 'Total amount');

  static String get locationTitle =>
      AppLanguage.pick(fr: 'Lieu', en: 'Location');

  static String get feeDetails =>
      AppLanguage.pick(fr: 'Détail du montant', en: 'Price details');

  static String get electricFee =>
      AppLanguage.pick(fr: 'Électricité', en: 'Electric fee');

  static String get vat => AppLanguage.pick(fr: 'TVA', en: 'VAT');

  static String get serviceFee =>
      AppLanguage.pick(fr: 'Frais de service', en: 'Service fee');

  static String get pricePerKwh =>
      AppLanguage.pick(fr: 'Prix du kWh', en: 'Price per kWh');

  static String get stopReason =>
      AppLanguage.pick(fr: 'Motif d’arrêt', en: 'Stop reason');

  static String get interruptedReason =>
      AppLanguage.pick(fr: 'Recharge interrompue', en: 'Charge interrupted');

  static String stopReasonOf(String? reason, bool running) {
    if (running) return '--';
    return switch (reason) {
      'PowerLoss' || 'Reboot' || 'HardReset' || 'SoftReset' => AppLanguage.pick(
        fr: 'Coupure d’électricité',
        en: 'Power cut',
      ),
      'Remote' ||
      'Local' ||
      'EVDisconnected' ||
      'EmergencyStop' ||
      'DeAuthorized' ||
      'UnlockCommand' => AppLanguage.pick(
        fr: 'Arrêt par le conducteur',
        en: 'Stopped by the driver',
      ),
      _ => AppLanguage.pick(fr: 'Montant atteint', en: 'Amount reached'),
    };
  }

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
    AppLanguage.pick(fr: 'Interrompues', en: 'Interrupted'),
  ];
}
