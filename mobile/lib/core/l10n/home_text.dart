import 'app_language.dart';

abstract final class HomeText {
  static String get greetingSubtitle => AppLanguage.pick(
    fr: 'Trouvez une borne, rechargez et partez.',
    en: 'Find a station, charge and go.',
  );

  static String get heroTitle => AppLanguage.pick(
    fr: 'Votre prochaine recharge est à portée de main',
    en: 'Your next charge is just a tap away',
  );

  static String get findStation =>
      AppLanguage.pick(fr: 'Trouver une borne', en: 'Find Station');

  static String get scanQr => AppLanguage.pick(fr: 'Scanner', en: 'Scan QR');

  static String get map => AppLanguage.pick(fr: 'Carte', en: 'Map');

  static String get activity => AppLanguage.pick(fr: 'Activité', en: 'History');

  static String get settings =>
      AppLanguage.pick(fr: 'Réglages', en: 'Settings');

  static String get notifications =>
      AppLanguage.pick(fr: 'Notifications', en: 'Notifications');
}
