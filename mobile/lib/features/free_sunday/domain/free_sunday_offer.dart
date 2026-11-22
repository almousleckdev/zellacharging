import 'free_sunday_window.dart';

class FreeSundayOffer {
  const FreeSundayOffer({
    required this.available,
    required this.reason,
    this.endsAt,
    this.window,
  });

  static const unavailable = FreeSundayOffer(
    available: false,
    reason: 'UNAVAILABLE',
  );

  static const _outsideProgram = {'NOT_LINKED', 'NO_AGREEMENT', 'UNAVAILABLE'};

  static const closingWindow = Duration(minutes: 30);

  final bool available;
  final String reason;
  final DateTime? endsAt;
  final FreeSundayWindow? window;

  bool get inProgram => available || !_outsideProgram.contains(reason);

  bool closesSoon(DateTime now) {
    final end = endsAt;
    if (!available || end == null) return false;
    final remaining = end.difference(now);
    return remaining > Duration.zero && remaining <= closingWindow;
  }
}
