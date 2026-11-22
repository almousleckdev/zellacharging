import '../domain/free_sunday_offer.dart';
import '../domain/free_sunday_window.dart';

FreeSundayOffer freeSundayOfferFromJson(Map<String, dynamic> json) {
  final endsAt = json['endsAt'] as String?;
  return FreeSundayOffer(
    available: json['available'] as bool,
    reason: json['reason'] as String,
    endsAt: endsAt == null ? null : DateTime.parse(endsAt),
    window: _windowFrom(json['window']),
  );
}

FreeSundayWindow? _windowFrom(Object? raw) {
  if (raw is! Map<String, dynamic>) return null;
  final day = raw['day'];
  final from = raw['from'];
  final until = raw['until'];
  final endOfDay = raw['endOfDay'];
  if (day is! String || from is! String || until is! String) return null;
  return FreeSundayWindow(
    day: day,
    from: from,
    until: until,
    endOfDay: endOfDay == true,
  );
}
