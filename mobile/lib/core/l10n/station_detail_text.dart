import 'app_language.dart';

abstract final class StationDetailText {
  static String get online => AppLanguage.pick(fr: 'En ligne', en: 'Online');

  static String get infoTitle =>
      AppLanguage.pick(fr: 'Informations', en: 'Information');

  static String get status => AppLanguage.pick(fr: 'Statut', en: 'Status');

  static String get lastSeen =>
      AppLanguage.pick(fr: 'Dernière activité', en: 'Last seen');

  static String get manufacturer =>
      AppLanguage.pick(fr: 'Fabricant', en: 'Manufacturer');

  static String get model => AppLanguage.pick(fr: 'Modèle', en: 'Model');

  static String get country => AppLanguage.pick(fr: 'Pays', en: 'Country');

  static String get coordinates =>
      AppLanguage.pick(fr: 'Coordonnées', en: 'Coordinates');

  static String get location =>
      AppLanguage.pick(fr: 'Localisation', en: 'Location');

  static String get directions =>
      AppLanguage.pick(fr: 'Itinéraire', en: 'Get directions');

  static String get directionsFailed => AppLanguage.pick(
    fr: "Impossible d'ouvrir l'application de cartes.",
    en: 'Could not open the maps app.',
  );

  static String availableOf(int available, int total) => AppLanguage.pick(
    fr: '$available sur $total disponibles',
    en: '$available of $total available',
  );

  static String countryName(String isoCode) => switch (isoCode) {
    'GA' => 'Gabon',
    _ => isoCode,
  };
}
