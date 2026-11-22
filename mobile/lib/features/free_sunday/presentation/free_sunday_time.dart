import '../../../core/format/date_format.dart';
import '../domain/free_sunday_offer.dart';

String? freeSundayEndTime(FreeSundayOffer offer) {
  final endsAt = offer.endsAt;
  if (endsAt == null) return null;
  final window = offer.window;
  if (window != null && window.endOfDay) return window.until;
  return DateFormatter.gabonTime(endsAt);
}
