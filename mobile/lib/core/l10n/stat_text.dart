import 'app_language.dart';

abstract final class StatText {
  static String get chargesTitle =>
      AppLanguage.pick(fr: 'Mes recharges', en: 'My charges');

  static String get energyTitle =>
      AppLanguage.pick(fr: 'Énergie consommée', en: 'Energy used');

  static String get spentTitle =>
      AppLanguage.pick(fr: 'Mes dépenses', en: 'My spending');

  static List<String> get periods => [
    AppLanguage.pick(fr: 'Jour', en: 'Day'),
    AppLanguage.pick(fr: 'Semaine', en: 'Week'),
    AppLanguage.pick(fr: 'Mois', en: 'Month'),
    AppLanguage.pick(fr: 'Tout', en: 'All'),
  ];

  static List<String> get periodCaptions => [
    AppLanguage.pick(fr: 'Aujourd’hui', en: 'Today'),
    AppLanguage.pick(fr: '7 derniers jours', en: 'Last 7 days'),
    AppLanguage.pick(fr: 'Ce mois-ci', en: 'This month'),
    AppLanguage.pick(fr: 'Depuis le début', en: 'Since the beginning'),
  ];

  static bool get _french => AppLanguage.current == AppLanguage.fr;

  static List<String> get weekdays => _french
      ? ['L', 'M', 'M', 'J', 'V', 'S', 'D']
      : ['M', 'T', 'W', 'T', 'F', 'S', 'S'];

  static List<String> get months => _french
      ? [
          'janv.',
          'févr.',
          'mars',
          'avr.',
          'mai',
          'juin',
          'juil.',
          'août',
          'sept.',
          'oct.',
          'nov.',
          'déc.',
        ]
      : [
          'Jan',
          'Feb',
          'Mar',
          'Apr',
          'May',
          'Jun',
          'Jul',
          'Aug',
          'Sep',
          'Oct',
          'Nov',
          'Dec',
        ];

  static String get totalCharges =>
      AppLanguage.pick(fr: 'Recharges', en: 'Charges');

  static String get totalEnergy =>
      AppLanguage.pick(fr: 'Énergie', en: 'Energy');

  static String get totalSpent => AppLanguage.pick(fr: 'Dépensé', en: 'Spent');

  static String get averagePerCharge =>
      AppLanguage.pick(fr: 'Moyenne par recharge', en: 'Average per charge');

  static String get bestPeriod =>
      AppLanguage.pick(fr: 'Pic de la période', en: 'Peak of the period');

  static String get chargesInPeriod => AppLanguage.pick(
    fr: 'Recharges de la période',
    en: 'Charges in this period',
  );

  static String get empty => AppLanguage.pick(
    fr: 'Aucune recharge sur cette période.',
    en: 'No charges in this period.',
  );

  static String get failedNote => AppLanguage.pick(
    fr: 'Les recharges non terminées ne comptent ni dans l’énergie ni dans les dépenses.',
    en: 'Unfinished charges are not counted in energy or spending.',
  );
}
