export 'auth_text.dart';
export 'charge_text.dart';
export 'error_text.dart';
export 'history_text.dart';
export 'profile_text.dart';
export 'station_text.dart';
export 'wallet_text.dart';

import 'app_language.dart';

abstract final class Strings {
  static String get appName => AppLanguage.pick(fr: 'Ndzela', en: 'Ndzela');

  static String get tagline => AppLanguage.pick(
    fr: 'Rechargez partout, sans souci',
    en: 'Charge anywhere, worry-free',
  );

  static String get home => AppLanguage.pick(fr: 'Accueil', en: 'Home');

  static String get locations =>
      AppLanguage.pick(fr: 'Stations', en: 'Stations');

  static String get charge => AppLanguage.pick(fr: 'Recharge', en: 'Charge');

  static String get history => AppLanguage.pick(fr: 'Activité', en: 'Activity');

  static String get profile => AppLanguage.pick(fr: 'Profil', en: 'Profile');

  static String get viewAll =>
      AppLanguage.pick(fr: 'Voir tout', en: 'View all');

  static String get retry => AppLanguage.pick(fr: 'Réessayer', en: 'Retry');

  static String get cancel => AppLanguage.pick(fr: 'Annuler', en: 'Cancel');

  static String get greeting => AppLanguage.pick(fr: 'Bonjour', en: 'Hello');

  static String get email => AppLanguage.pick(fr: 'E-mail', en: 'Email');

  static String get phone => AppLanguage.pick(fr: 'Téléphone', en: 'Phone');

  static String get required => AppLanguage.pick(
    fr: 'Ce champ est obligatoire',
    en: 'This field is required',
  );

  static String get invalidEmail => AppLanguage.pick(
    fr: 'Saisissez une adresse e-mail valide',
    en: 'Enter a valid email address',
  );

  static String get invalidCode => AppLanguage.pick(
    fr: 'Saisissez le code à 6 chiffres',
    en: 'Enter the 6-digit code',
  );

  static String get shortPassword => AppLanguage.pick(
    fr: 'Au moins 8 caractères',
    en: 'At least 8 characters',
  );

  static String get contactRequired => AppLanguage.pick(
    fr: 'Saisissez un e-mail ou un numéro de téléphone',
    en: 'Enter an email or a phone number',
  );

  static String get unknownError => AppLanguage.pick(
    fr: 'Une erreur est survenue. Veuillez réessayer.',
    en: 'Something went wrong. Please try again.',
  );

  static String get networkError => AppLanguage.pick(
    fr: 'Impossible de joindre le serveur. Vérifiez votre connexion.',
    en: 'Cannot reach the server. Check your connection.',
  );

  static String get next => AppLanguage.pick(fr: 'Continuer', en: 'Continue');

  static String get getStarted =>
      AppLanguage.pick(fr: 'Commencer', en: 'Get started');

  static const distanceUnit = 'km';
  static const powerUnit = 'kW';
  static const kwhUnit = 'kWh';
  static const voltageUnit = 'V';
  static const currentUnit = 'A';
}
