import '../../charging/domain/charging_session.dart';
import '../../charging/domain/session_state.dart';
import 'stat_bucket.dart';
import 'stat_metric.dart';
import 'stat_period.dart';
import 'stat_report.dart';

abstract final class ActivityStats {
  static const _hoursPerDayBucket = 3;
  static const _allBuckets = 12;

  static StatReport report(
    List<ChargingSession> sessions,
    StatMetric metric,
    StatPeriod period,
    DateTime now,
  ) {
    final today = DateTime(now.year, now.month, now.day);
    final starts = _starts(period, today, sessions);
    final inRange = [
      for (final session in sessions)
        if (!_timeOf(session).isBefore(starts.first) &&
            _timeOf(session).isBefore(_end(period, starts.last, today)))
          session,
    ]..sort((a, b) => _timeOf(b).compareTo(_timeOf(a)));
    final done = inRange.where((s) => s.state == SessionState.completed);
    final priced = done.where((s) => s.costMinor != null).toList();
    return StatReport(
      metric: metric,
      period: period,
      buckets: [
        for (var i = 0; i < starts.length; i++)
          StatBucket(
            start: starts[i],
            value: _value(
              metric,
              inRange,
              starts[i],
              i + 1 < starts.length
                  ? starts[i + 1]
                  : _end(period, starts[i], today),
            ),
          ),
      ],
      sessions: inRange,
      charges: inRange.length,
      energyKwh: done.fold(0.0, (sum, s) => sum + (s.shownEnergyKwh ?? 0)),
      spentMinor: priced.fold(0, (sum, s) => sum + s.costMinor!),
      pricedCharges: priced.length,
      currency: priced.isEmpty ? null : priced.first.currency,
      currencyDecimals: priced.isEmpty ? 0 : priced.first.currencyDecimals ?? 0,
    );
  }

  static DateTime _timeOf(ChargingSession session) =>
      (session.startedAt ?? session.createdAt).toLocal();

  static double _value(
    StatMetric metric,
    List<ChargingSession> sessions,
    DateTime from,
    DateTime until,
  ) {
    final inside = sessions.where(
      (s) => !_timeOf(s).isBefore(from) && _timeOf(s).isBefore(until),
    );
    final done = inside.where((s) => s.state == SessionState.completed);
    return switch (metric) {
      StatMetric.charges => inside.length.toDouble(),
      StatMetric.energy => done.fold(
        0.0,
        (sum, s) => sum + (s.shownEnergyKwh ?? 0),
      ),
      StatMetric.spent => done.fold(0.0, (sum, s) => sum + (s.costMinor ?? 0)),
    };
  }

  static List<DateTime> _starts(
    StatPeriod period,
    DateTime today,
    List<ChargingSession> sessions,
  ) => switch (period) {
    StatPeriod.day => [
      for (var hour = 0; hour < 24; hour += _hoursPerDayBucket)
        DateTime(today.year, today.month, today.day, hour),
    ],
    StatPeriod.week => [
      for (var back = 6; back >= 0; back--)
        DateTime(today.year, today.month, today.day - back),
    ],
    StatPeriod.month => [
      for (
        var day = 1;
        day <= DateTime(today.year, today.month + 1, 0).day;
        day++
      )
        DateTime(today.year, today.month, day),
    ],
    StatPeriod.all => _months(today, sessions),
  };

  static List<DateTime> _months(
    DateTime today,
    List<ChargingSession> sessions,
  ) {
    final earliest = sessions.isEmpty
        ? today
        : sessions.map(_timeOf).reduce((a, b) => a.isBefore(b) ? a : b);
    final first = DateTime(earliest.year, earliest.month);
    final span = (today.year - first.year) * 12 + today.month - first.month + 1;
    final count = span > _allBuckets ? _allBuckets : span;
    return [
      for (var i = count - 1; i >= 0; i--)
        DateTime(today.year, today.month - i),
    ];
  }

  static DateTime _end(StatPeriod period, DateTime lastStart, DateTime today) =>
      switch (period) {
        StatPeriod.day => DateTime(
          lastStart.year,
          lastStart.month,
          lastStart.day,
          lastStart.hour + _hoursPerDayBucket,
        ),
        StatPeriod.week || StatPeriod.month => DateTime(
          lastStart.year,
          lastStart.month,
          lastStart.day + 1,
        ),
        StatPeriod.all => DateTime(lastStart.year, lastStart.month + 1),
      };
}
