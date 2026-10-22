import 'app_language.dart';

abstract final class StationText {
  static String get nearbyStations =>
      AppLanguage.pick(fr: 'Stations proches', en: 'Nearby stations');

  static String get exploreLocations =>
      AppLanguage.pick(fr: 'Explorer les stations', en: 'Explore stations');

  static String get searchStations => AppLanguage.pick(
    fr: 'Rechercher un lieu ou une borne',
    en: 'Search a place or charger',
  );

  static String get noStations => AppLanguage.pick(
    fr: 'Aucune station de recharge à proximité.',
    en: 'No charging stations found nearby.',
  );

  static String get stationOffline =>
      AppLanguage.pick(fr: 'Hors ligne', en: 'Offline');

  static String get available =>
      AppLanguage.pick(fr: 'Disponible', en: 'Available');

  static String get connectors =>
      AppLanguage.pick(fr: 'Connecteurs', en: 'Connectors');

  static String get connector =>
      AppLanguage.pick(fr: 'Connecteur', en: 'Connector');

  static String get maxPower =>
      AppLanguage.pick(fr: 'Puissance max.', en: 'Max power');

  static String get stationDetails =>
      AppLanguage.pick(fr: 'Station', en: 'Station');

  static String get scanTitle => AppLanguage.pick(
    fr: 'Scanner le QR code de la borne',
    en: 'Scan the charger QR code',
  );

  static String get scanHint => AppLanguage.pick(
    fr: 'Pointez la caméra vers le QR code de la borne.',
    en: 'Point the camera at the QR code on the charger.',
  );

  static String get cameraDenied => AppLanguage.pick(
    fr: 'L\'accès à la caméra est nécessaire pour scanner les QR codes.',
    en: 'Camera access is required to scan QR codes.',
  );

  static String get plugInFirst => AppLanguage.pick(
    fr: 'Branchez d\'abord votre véhicule au connecteur, puis appuyez sur Démarrer.',
    en: 'Plug your vehicle into the connector first, then press Start.',
  );

  static String get startCharging =>
      AppLanguage.pick(fr: 'Démarrer', en: 'Start');

  static String get chooseConnector => AppLanguage.pick(
    fr: 'Choisissez un connecteur',
    en: 'Choose a connector',
  );

  static String get noConnectorAvailable => AppLanguage.pick(
    fr: 'Aucun connecteur n\'est disponible pour le moment.',
    en: 'No connector is available right now.',
  );

  static String get selectConnector => AppLanguage.pick(
    fr: 'Choisissez d\'abord un connecteur',
    en: 'Select a connector first',
  );

  static String get connectorBusy => AppLanguage.pick(
    fr: 'Ce connecteur est occupé. Choisissez-en un autre.',
    en: 'This connector is in use. Choose another one.',
  );

  static String get connectorTaken => AppLanguage.pick(
    fr: 'Le connecteur choisi vient d\'être occupé.',
    en: 'The connector you selected is now in use.',
  );

  static Map<String, String> get statusLabels => {
    'Available': available,
    'Preparing': AppLanguage.pick(fr: 'En préparation', en: 'Preparing'),
    'Charging': busy,
    'SuspendedEV': busy,
    'SuspendedEVSE': busy,
    'Finishing': AppLanguage.pick(fr: 'Fin de recharge', en: 'Finishing'),
    'Reserved': AppLanguage.pick(fr: 'Réservé', en: 'Reserved'),
    'Unavailable': AppLanguage.pick(fr: 'Indisponible', en: 'Unavailable'),
    'Faulted': AppLanguage.pick(fr: 'Hors service', en: 'Out of order'),
  };

  static String get busy => AppLanguage.pick(fr: 'Occupé', en: 'In use');
}
