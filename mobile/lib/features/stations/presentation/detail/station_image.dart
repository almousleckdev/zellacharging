abstract final class StationImage {
  static const fallback = 'assets/images/bornes.png';

  static String? forCountry(String? isoCode) => switch (isoCode) {
    'GA' => 'assets/images/stationgabon.png',
    _ => null,
  };
}
