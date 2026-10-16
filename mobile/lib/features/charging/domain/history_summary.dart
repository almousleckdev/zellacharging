import 'charging_session.dart';
import 'session_state.dart';

class HistorySummary {
  const HistorySummary({
    required this.sessions,
    required this.energyKwh,
    required this.spentMinor,
    required this.currency,
    required this.currencyDecimals,
  });

  factory HistorySummary.of(List<ChargingSession> all) {
    final done = all.where((s) => s.state == SessionState.completed).toList();
    final priced = done.where((s) => s.costMinor != null).toList();
    return HistorySummary(
      sessions: all.length,
      energyKwh: done.fold(0.0, (sum, s) => sum + (s.shownEnergyKwh ?? 0)),
      spentMinor: priced.fold(0, (sum, s) => sum + s.costMinor!),
      currency: priced.isEmpty ? null : priced.first.currency,
      currencyDecimals: priced.isEmpty ? 0 : priced.first.currencyDecimals ?? 0,
    );
  }

  final int sessions;
  final double energyKwh;
  final int spentMinor;
  final String? currency;
  final int currencyDecimals;
}
